package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultLeaseReturnEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultLeaseReturnEntry("code-349", "name-312")
        val view = toDefaultLeaseReturnEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultLeaseReturnEntryViews(mutableListOf<DefaultLeaseReturnEntry>(item, item)).size, "views")
    }
}
