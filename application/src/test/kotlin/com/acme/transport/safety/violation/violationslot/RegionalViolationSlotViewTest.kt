package com.acme.transport.safety.violation.violationslot

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalViolationSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalViolationSlot("code-407", "label-300")
        val view = toRegionalViolationSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalViolationSlotViews(mutableListOf<RegionalViolationSlot>(item, item)).size, "views")
    }
}
