package com.acme.transport.safety.violation.manualviolationquota

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackViolationSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackViolationSlot("code-228", "title-82")
        val view = toFallbackViolationSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackViolationSlotViews(mutableListOf<FallbackViolationSlot>(item, item)).size, "views")
    }
}
