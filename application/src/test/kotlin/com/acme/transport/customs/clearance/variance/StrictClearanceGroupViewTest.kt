package com.acme.transport.customs.clearance.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictClearanceGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictClearanceGroup("code-214", "label-433")
        val view = toStrictClearanceGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictClearanceGroupViews(mutableListOf<StrictClearanceGroup>(item, item)).size, "views")
    }
}
