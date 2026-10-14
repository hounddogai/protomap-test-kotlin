package com.acme.transport.customs.clearance.target

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryClearanceGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryClearanceGroup("code-422", "name-751")
        val view = toPrimaryClearanceGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryClearanceGroupViews(mutableListOf<PrimaryClearanceGroup>(item, item)).size, "views")
    }
}
