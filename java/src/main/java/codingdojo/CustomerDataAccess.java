package codingdojo;

public class CustomerDataAccess {

    private final CustomerDataLayer customerDataLayer;

    public CustomerDataAccess(CustomerDataLayer customerDataLayer) {
        this.customerDataLayer = customerDataLayer;
    }

    public Customer findCompanyByExternalIdOrCompanyNumber(String externalId, String companyNumber) {
        Customer customer = findCustomer(CustomerType.COMPANY, externalId);
        if (customer != null) {
            return customer;
        }

        customer = customerDataLayer.findByCompanyNumber(companyNumber);
        if (customer != null) {
            String customerExternalId = customer.getExternalId();
            if (customerExternalId != null && !externalId.equals(customerExternalId)) {
                throw new ConflictException("Existing customer for externalCustomer " + companyNumber
                        + " doesn't match external id " + externalId + " instead found " + customerExternalId);
            }
        }

        return customer;
    }

    public Customer findPerson(String externalId) {
        return findCustomer(CustomerType.PERSON, externalId);
    }

    private Customer findCustomer(CustomerType customerType, String externalId) {
        final Customer customer = customerDataLayer.findByExternalId(externalId);

        if (customer != null && customerType != customer.getCustomerType()) {
            throw new ConflictException("Existing customer for externalCustomer " + externalId + " already exists and is not a " + customerType);
        }

        return customer;
    }

    public Customer updateCustomerRecord(Customer customer) {
        return customerDataLayer.updateCustomerRecord(customer);
    }

    public Customer createCustomerRecord(Customer customer) {
        return customerDataLayer.createCustomerRecord(customer);
    }

    public Customer findByMasterExternalId(String masterExternalId) {
        return customerDataLayer.findByMasterExternalId(masterExternalId);
    }

    public void updateShoppingList(Customer customer, ShoppingList consumerShoppingList) {
        customer.addShoppingList(consumerShoppingList);
        customerDataLayer.updateShoppingList(consumerShoppingList);
        customerDataLayer.updateCustomerRecord(customer);
    }

    public Customer saveCustomerRecord(Customer customer) {
        if (customer.getInternalId() == null ) {
            return createCustomerRecord(customer);
        } else {
            return updateCustomerRecord(customer);
        }
    }

}
