package com.acme.transport.customs.clearance.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryClearanceProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryClearanceProfile("code-67", "name-113")
        val view = toPrimaryClearanceProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryClearanceProfileViews(mutableListOf<PrimaryClearanceProfile>(item, item)).size, "views")
    }
}
