package com.acme.transport.customs.clearance.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyClearanceGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyClearanceGroup("code-988", "label-891")
        val view = toLegacyClearanceGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyClearanceGroupViews(mutableListOf<LegacyClearanceGroup>(item, item)).size, "views")
    }
}
