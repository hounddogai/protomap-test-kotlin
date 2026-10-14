package com.acme.transport.customs.clearance.request

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyClearanceWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyClearanceWindow("code-810", "label-459")
        val view = toLegacyClearanceWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyClearanceWindowViews(mutableListOf<LegacyClearanceWindow>(item, item)).size, "views")
    }
}
