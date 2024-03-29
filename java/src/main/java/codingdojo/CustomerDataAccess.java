package codingdojo;

public class CustomerDataAccess {

    private final CustomerDataLayer customerDataLayer;

    public CustomerDataAccess(CustomerDataLayer customerDataLayer) {
        this.customerDataLayer = customerDataLayer;
    }

    public Customer loadCompanyCustomer(String externalId, String companyNumber) {
        // TODO check if companyNumber matches with founded by externalId
        Customer matchByExternalId = this.customerDataLayer.findByExternalId(externalId);
        if (matchByExternalId != null) {
            return matchByExternalId;
        } else {
            Customer matchByCompanyNumber = this.customerDataLayer.findByCompanyNumber(companyNumber);
            return matchByCompanyNumber;
        }
    }

    public Customer loadCompanyByMasterExternalId(String externalId) {
        // TODO check about DataLayer and DataAccess 
        return this.customerDataLayer.findByMasterExternalId(externalId);
    }

    public Customer loadPersonCustomer(String externalId) {
        Customer matchByPersonalNumber = this.customerDataLayer.findByExternalId(externalId);
        return matchByPersonalNumber;
    }

    public Customer updateCustomerRecord(Customer customer) {
        return customerDataLayer.updateCustomerRecord(customer);
    }

    public Customer createCustomerRecord(Customer customer) {
        return customerDataLayer.createCustomerRecord(customer);
    }

    public void updateShoppingList(Customer customer, ShoppingList consumerShoppingList) {
        customer.addShoppingList(consumerShoppingList);
        customerDataLayer.updateShoppingList(consumerShoppingList);
        customerDataLayer.updateCustomerRecord(customer);
    }
}
