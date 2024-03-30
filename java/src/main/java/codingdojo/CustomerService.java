package codingdojo;

public class CustomerService {

    final CustomerDataLayer customerDataLayer;

    public CustomerService(CustomerDataLayer customerDataLayer) {
        this.customerDataLayer = customerDataLayer;
    }

    public Customer loadCompany(ExternalCustomer externalCustomer) {
        // TODO check for inputs,
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

    public Customer loadCompanyByMasterExternalId(String externalId) {
        return this.customerDataLayer.findByMasterExternalId(externalId);
    }

    public Customer saveCustomer(Customer customer) {
        return customerDataLayer.saveCustomerRecord(customer);
    }

    public Customer createCustomer(ExternalCustomer externalCustomer) {
        final Customer customer = new Customer();
        customer.setExternalId(externalCustomer.getExternalId());
        customer.setMasterExternalId(externalCustomer.getExternalId());
        return customer;
    }

    public void updateShoppingList(Customer customer, ShoppingList consumerShoppingList) {
        customer.addShoppingList(consumerShoppingList);
        customerDataLayer.updateShoppingList(consumerShoppingList);
        customerDataLayer.updateCustomerRecord(customer);
    }
}
