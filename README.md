Consumer Match Kata 
====================

Some horrible code to refactor. Concentrate on the "CustomerSync" class. The purpose of the 'syncWithDataLayer' method is to take a ExternalCustomer instance, which has been updated in an external system, and see whether there is a matching Customer in our database. If there is not, create a new Customer to match the incoming ExternalCustomer. If there is one, update it. If there are several matching Customers in our database, update them all (slightly differently).

There is a unit test there to start you off. It gives you a basic amount of coverage but has a rather weak assertion.

The change you need to make
---------------------------

As ever, you have a goal with your refactoring. The scenario is that you have been asked to synchronize an additional field from the ExternalCustomer to the Customer. The field is 'bonusPointsBalance' and is an integer. Only private people have bonus points, not companies. Add the field and ensure that if the ExternalCustomer has a different number of points from the Customer, the balance is updated in our database.

Branch with_tests
-----------------

The branch 'with_tests' is an alternative starting point where there are good unit tests available, and you can get started refactoring straight away. The code coverage is not quite 100%, I believe this is due to unreachable code. Another way to use this code is to read and understand the approval testing techniques used, or to re-write the tests in another style.


Changes by author: Boris Kukec
-------------------------

The branch `refactor_with_tests`, and corresponding pull request, is based on a branch `with_tests`. I decided to use branch `with_tests` because for refactorings, test coverage is a must.
Besides significant refactor, a feature `bonusPointsBalance` as stated above, was added.
Some of the existing tests ware slightly expanded, assertions for `bonusPointsBalance` ware added. One new test was added.
In the refactoring part, I kept the original layers.
`DataAccess` class now returns only `Customer` objects.
`Sync` class contains only business rules: how to interpret found costumers and how to merge them.
`CustomerMatches` class is now only used inside `Sync` class, and was reduced since it was obvious that property `matchTerm` was no longer needed.