package com.acme.transport.fleetops.leasereturn.returnpreference

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingLeaseReturnPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingLeaseReturnPreference("code-402", "title-460")
        val view = toPendingLeaseReturnPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingLeaseReturnPreferenceViews(mutableListOf<PendingLeaseReturnPreference>(item, item)).size, "views")
    }
}
