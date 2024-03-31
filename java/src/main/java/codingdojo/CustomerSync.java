package codingdojo;

import java.util.List;

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
            customerMatches.getDuplicates().forEach(duplciate -> updateDuplicate(externalCustomer, duplciate));
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
        customer.setName(externalCustomer.getName());
        if (externalCustomer.isCompany()) {
            customer.setCompanyNumber(externalCustomer.getCompanyNumber());
            customer.setCustomerType(CustomerType.COMPANY);
        } else {
            customer.setCustomerType(CustomerType.PERSON);
        }
    }

    private void updateContactInfo(ExternalCustomer externalCustomer, Customer customer) {
        customer.setAddress(externalCustomer.getAddress());
    }

    public CustomerMatches findCustomerMatches(ExternalCustomer externalCustomer) {

        if (externalCustomer.isCompany()) {
            return findCompanyMatches(externalCustomer);
        }
        // otherwise load the customer as a person and return a simple customer match
        final Customer customer = customerDataAccess.loadPersonCustomer(externalCustomer);
        final CustomerMatches customerMatches = new CustomerMatches();
        customerMatches.setCustomer(customer);
        return customerMatches;
    }

    private CustomerMatches findCompanyMatches(ExternalCustomer externalCustomer) {

        final Customer customer = customerDataAccess.loadCompanyCustomer(externalCustomer);        
        if (customer == null) {
            return new CustomerMatches();
        }

        final String externalId = externalCustomer.getExternalId();
        final String companyNumber = externalCustomer.getCompanyNumber();
        final String customerExternalId = customer.getExternalId();
        final CustomerMatches customerMatches = new CustomerMatches();

        if (customerExternalId != null) {
            final String customerCompanyNumber = customer.getCompanyNumber();
            if (!companyNumber.equals(customerCompanyNumber)) { // company number does not match add as duplicate
                customerMatches.addDuplicate(customer);
            } else {
                customerMatches.setCustomer(customer);
            }
            final Customer customerByMaster = customerDataAccess.findByMasterExternalId(externalId);
            if (customerByMaster != null) { // customer found by masterExternalId, add it as a duplciate
                customerMatches.addDuplicate(customerByMaster);
            }
        } else {
            customer.setExternalId(externalId);
            customer.setMasterExternalId(externalId);
            customerMatches.setCustomer(customer);
        }
        return customerMatches;
    }

}
