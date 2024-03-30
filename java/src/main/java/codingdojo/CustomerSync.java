package codingdojo;

import java.util.List;

public class CustomerSync {

    private final CustomerService customerService;

    public CustomerSync(CustomerDataLayer customerDataLayer) {
        this(new CustomerService(customerDataLayer));
    }

    public CustomerSync(CustomerService customerService) {
        this.customerService = customerService;
    }

    public boolean syncWithDataLayer(ExternalCustomer externalCustomer) {

        CustomerMatches customerMatches = findCustomerMatches(externalCustomer);
        Customer customer = customerMatches.getCustomer();
        boolean created = false;

        if (customer == null) {
            customer = createCustomer(externalCustomer);
            created = true;
        }

        populateFields(externalCustomer, customer);

        updateContactInfo(externalCustomer, customer);

        if (customerMatches.hasDuplicates()) {
            for (Customer duplicate : customerMatches.getDuplicates()) {
                updateDuplicate(externalCustomer, duplicate);
            }
        }

        updateRelations(externalCustomer, customer);

        updatePreferredStore(externalCustomer, customer);

        customerService.saveCustomer(customer);
        return created;
    }

    public Customer createCustomer(ExternalCustomer externalCustomer) {
        final Customer customer = new Customer();
        customer.setExternalId(externalCustomer.getExternalId());
        customer.setMasterExternalId(externalCustomer.getExternalId());
        return customer;
    }

    private void updateRelations(ExternalCustomer externalCustomer, Customer customer) {
        List<ShoppingList> consumerShoppingLists = externalCustomer.getShoppingLists();
        for (ShoppingList consumerShoppingList : consumerShoppingLists) {
            customerService.updateShoppingList(customer, consumerShoppingList);
        }
    }

    private void updateDuplicate(ExternalCustomer externalCustomer, Customer duplicate) {
        if (duplicate == null) {
            duplicate = new Customer();
            duplicate.setExternalId(externalCustomer.getExternalId());
            duplicate.setMasterExternalId(externalCustomer.getExternalId());
        }

        duplicate.setName(externalCustomer.getName());

        customerService.saveCustomer(duplicate);
    }

    private void updatePreferredStore(ExternalCustomer externalCustomer, Customer customer) {
        customer.setPreferredStore(externalCustomer.getPreferredStore());
    }

    // TODO createCustomer(Externalcustomer)

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
        customer.setAddress(externalCustomer.getPostalAddress());
    }

    public CustomerMatches findCustomerMatches(ExternalCustomer externalCustomer) {

        if (externalCustomer.isCompany()) {
            return findCompanyMatches(externalCustomer);
        }
        // otherwise load a person and return a simple customer match
        final Customer customer = customerService.loadPerson(externalCustomer);
        CustomerMatches customerMatches = new CustomerMatches();
        customerMatches.setCustomer(customer);
        return customerMatches;
    }

    private CustomerMatches findCompanyMatches(ExternalCustomer externalCustomer) {
        final String externalId = externalCustomer.getExternalId();
        final String companyNumber = externalCustomer.getCompanyNumber();
        final Customer customer = customerService.loadCompany(externalCustomer);
        
        if (customer == null) {
            return new CustomerMatches();
        }

        CustomerMatches customerMatches = new CustomerMatches();
        String customerExternalId = customer.getExternalId();
        if (customerExternalId != null) {
            String customerCompanyNumber = customer.getCompanyNumber();
            if (!companyNumber.equals(customerCompanyNumber)) { // company number does not match add as duplicate
                customerMatches.addDuplicate(customer);
            } else {
                customerMatches.setCustomer(customer);
            }
            Customer customerByMaster = customerService.loadCompanyByMasterExternalId(externalId);
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
