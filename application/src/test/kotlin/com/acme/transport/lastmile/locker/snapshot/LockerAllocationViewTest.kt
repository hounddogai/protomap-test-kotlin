package com.acme.transport.lastmile.locker.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class LockerAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLockerAllocation("code-861", "name-498")
        val view = toLockerAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLockerAllocationViews(mutableListOf<LockerAllocation>(item, item)).size, "views")
    }
}
