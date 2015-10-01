package org.jenkinsci.test.acceptance.po.users

import org.jenkinsci.test.acceptance.po.Page

/**
 * Page object for the user list page.
 *
 * @author christian.fritz
 */
class UserListPage extends Page {

    static url = "securityRealm"
    static at = { title == "Users [Jenkins]" }
    static content = {
        userNames {
            $("table#people>tbody>tr:not(:first-child)>td:nth-child(2)>a")*.text()
        }

        fullNames {
            $("table#people>tbody>tr:not(:first-child)>td:nth-child(3)>a")*.text()
        }

        configure {
            $("table#people>tbody>tr:not(:first-child)>td:nth-child(4)>a[href\$='configure']").collectEntries {
                def userName = it.attr("href").split("/")[-2]
                [userName, it]
            }
        }

        delete(to: DeleteUserPage) {
            $("table#people>tbody>tr:not(:first-child)>td:nth-child(4)>a[href\$='delete']").collectEntries {
                def userName = it.attr("href").split("/")[-2]
                [userName, it]
            }
        }
    }
}
