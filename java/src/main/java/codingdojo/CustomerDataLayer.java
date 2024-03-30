package codingdojo;

public interface CustomerDataLayer {

    default Customer saveCustomerRecord(Customer customer) {
        if (customer.getInternalId() == null ) {
            return createCustomerRecord(customer);
        } else {
            return updateCustomerRecord(customer);
        }
    }

    Customer updateCustomerRecord(Customer customer);

    Customer createCustomerRecord(Customer customer);

    void updateShoppingList(ShoppingList consumerShoppingList);

    Customer findByExternalId(String externalId);

    Customer findByMasterExternalId(String externalId);

    Customer findByCompanyNumber(String companyNumber);
}
