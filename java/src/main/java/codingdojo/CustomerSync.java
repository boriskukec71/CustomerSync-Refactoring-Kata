package codingdojo;

import java.util.List;

public class CustomerSync {

    private final CustomerDataLayer customerDataLayer;

    public CustomerSync(CustomerDataLayer customerDataLayer) {
        this.customerDataLayer = customerDataLayer;
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

        customerDataLayer.saveCustomerRecord(customer);
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
            updateShoppingList(customer, consumerShoppingList);
        }
    }

    private void updateDuplicate(ExternalCustomer externalCustomer, Customer duplicate) {
        if (duplicate == null) {
            duplicate = new Customer();
            duplicate.setExternalId(externalCustomer.getExternalId());
            duplicate.setMasterExternalId(externalCustomer.getExternalId());
        }

        duplicate.setName(externalCustomer.getName());

        customerDataLayer.saveCustomerRecord(duplicate);
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
        customer.setAddress(externalCustomer.getPostalAddress());
    }

    public CustomerMatches findCustomerMatches(ExternalCustomer externalCustomer) {

        if (externalCustomer.isCompany()) {
            return findCompanyMatches(externalCustomer);
        }
        // otherwise load a person and return a simple customer match
        final Customer customer = loadPerson(externalCustomer);
        CustomerMatches customerMatches = new CustomerMatches();
        customerMatches.setCustomer(customer);
        return customerMatches;
    }

    private CustomerMatches findCompanyMatches(ExternalCustomer externalCustomer) {
        final String externalId = externalCustomer.getExternalId();
        final String companyNumber = externalCustomer.getCompanyNumber();
        final Customer customer = loadCompany(externalCustomer);
        
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
            Customer customerByMaster = customerDataLayer.findByMasterExternalId(externalId);
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

    public Customer loadCompany(ExternalCustomer externalCustomer) {
        // TODO check for inputs
        final String externalId = externalCustomer.getExternalId();
        final String companyNumber = externalCustomer.getCompanyNumber();
        Customer customer = customerDataLayer.findByExternalId(externalId);

        if (customer == null) {
            customer = customerDataLayer.findByCompanyNumber(companyNumber);
            if (customer == null) {
                return null;
            }
            String customerExternalId = customer.getExternalId();
            if (customerExternalId != null && !externalId.equals(customerExternalId)) {
                throw new ConflictException("Existing customer for externalCustomer " + companyNumber + " doesn't match external id " + externalId + " instead found " + customerExternalId );
            }
        }

        if (customer != null && !CustomerType.COMPANY.equals(customer.getCustomerType())) {
            throw new ConflictException("Existing customer for externalCustomer " + externalId + " already exists and is not a company");
        }

        return customer;
    }

    public Customer loadPerson(ExternalCustomer externalCustomer) {
        final String externalId = externalCustomer.getExternalId();
        final Customer customer = customerDataLayer.findByExternalId(externalId);

        if (customer != null && !CustomerType.PERSON.equals(customer.getCustomerType())) {
            throw new ConflictException("Existing customer for externalCustomer " + externalId + " already exists and is not a person");
        }

        return customer;
    }

    public void updateShoppingList(Customer customer, ShoppingList consumerShoppingList) {
        customer.addShoppingList(consumerShoppingList);
        customerDataLayer.updateShoppingList(consumerShoppingList);
        customerDataLayer.updateCustomerRecord(customer);
    }

}
