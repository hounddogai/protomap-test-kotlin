package com.acme.transport.fleetops.leasereturn.returnentry

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyLeaseReturnEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyLeaseReturnEntry("code-605", "name-921")
        val view = toLegacyLeaseReturnEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyLeaseReturnEntryViews(mutableListOf<LegacyLeaseReturnEntry>(item, item)).size, "views")
    }
}
