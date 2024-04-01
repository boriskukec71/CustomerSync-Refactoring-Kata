package codingdojo;

import java.util.List;
import static java.util.Objects.requireNonNull;

public class CustomerSync {

    private final CustomerDataAccess customerDataAccess;

    public CustomerSync(CustomerDataLayer customerDataLayer) {
        this(new CustomerDataAccess(customerDataLayer));
    }

    public CustomerSync(CustomerDataAccess db) {
        this.customerDataAccess = db;
    }

    public boolean syncWithDataLayer(ExternalCustomer externalCustomer) {

        final CustomerMatches customerMatches = findCustomerMatches(externalCustomer);
        Customer customer = customerMatches.getCustomer();
        boolean created = false;

        if (customer == null) {
            customer = createCustomer(externalCustomer);
            created = true;
        }

        populateFields(externalCustomer, customer);

        updateContactInfo(externalCustomer, customer);

        if (customerMatches.hasDuplicates()) {
            customerMatches.getDuplicates().forEach(duplicate -> updateDuplicate(externalCustomer, duplicate));
        }

        updateRelations(externalCustomer, customer);

        updatePreferredStore(externalCustomer, customer);

        customerDataAccess.saveCustomerRecord(customer);
        return created;
    }

    public Customer createCustomer(ExternalCustomer externalCustomer) {
        final Customer customer = new Customer();
        customer.setExternalId(externalCustomer.getExternalId());
        customer.setMasterExternalId(externalCustomer.getExternalId());
        if (externalCustomer.isCompany()) {
            customer.setCustomerType(CustomerType.COMPANY);
            customer.setCompanyNumber(externalCustomer.getCompanyNumber());
        } else {
            customer.setCustomerType(CustomerType.PERSON);
        }
        return customer;
    }

    private void updateRelations(ExternalCustomer externalCustomer, Customer customer) {
        final List<ShoppingList> consumerShoppingLists = externalCustomer.getShoppingLists();
        for (ShoppingList consumerShoppingList : consumerShoppingLists) {
            customerDataAccess.updateShoppingList(customer, consumerShoppingList);
        }
    }

    private void updateDuplicate(ExternalCustomer externalCustomer, Customer duplicate) {
        duplicate.setName(externalCustomer.getName());
        customerDataAccess.saveCustomerRecord(duplicate);
    }

    private void updatePreferredStore(ExternalCustomer externalCustomer, Customer customer) {
        customer.setPreferredStore(externalCustomer.getPreferredStore());
    }

    private void populateFields(ExternalCustomer externalCustomer, Customer customer) {
        final String externalId = externalCustomer.getExternalId();
        customer.setName(externalCustomer.getName());
        if (externalCustomer.isCompany()) {
            if (customer.getExternalId() == null) {
                customer.setExternalId(externalId);
                customer.setMasterExternalId(externalId);
            }
        } else {
            customer.setBonusPointsBalance(externalCustomer.getBonusPointsBalance());
        }
    }

    private void updateContactInfo(ExternalCustomer externalCustomer, Customer customer) {
        customer.setAddress(externalCustomer.getAddress());
    }

    public CustomerMatches findCustomerMatches(ExternalCustomer externalCustomer) {
        if (externalCustomer.isCompany()) {
            return findCompanyMatches(externalCustomer);
        }
        return findPersonMatches(externalCustomer);
    }

    private CustomerMatches findCompanyMatches(ExternalCustomer externalCustomer) {
        final String externalId = externalCustomer.getExternalId();
        requireNonNull(externalId, "ExternalId number must not be null");
        final String companyNumber = externalCustomer.getCompanyNumber();
        requireNonNull(companyNumber, "Company number must not be null");

        final CustomerMatches customerMatches = new CustomerMatches();

        final Customer customer = customerDataAccess.findCompanyByExternalIdOrCompanyNumber(externalId, companyNumber);
        if (customer == null) {
            return customerMatches;
        }

        if (!companyNumber.equals(customer.getCompanyNumber())) {
            customerMatches.addDuplicate(customer);
        } else {
            customerMatches.setCustomer(customer);
        }

        final Customer customerByMaster = customerDataAccess.findByMasterExternalId(externalId);
        if (customerByMaster != null) {
            customerMatches.addDuplicate(customerByMaster);
        }

        return customerMatches;
    }

    private CustomerMatches findPersonMatches(ExternalCustomer externalCustomer) {
        final Customer customer = customerDataAccess.findPerson(externalCustomer.getExternalId());
        final CustomerMatches customerMatches = new CustomerMatches();
        customerMatches.setCustomer(customer);
        return customerMatches;
    }

}
