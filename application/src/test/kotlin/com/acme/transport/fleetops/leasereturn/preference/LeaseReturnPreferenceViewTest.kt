package com.acme.transport.fleetops.leasereturn.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class LeaseReturnPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLeaseReturnPreference("code-937", "name-524")
        val view = toLeaseReturnPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLeaseReturnPreferenceViews(mutableListOf<LeaseReturnPreference>(item, item)).size, "views")
    }
}
