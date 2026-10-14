package com.acme.transport.safety.violation.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalViolationTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalViolationTier("code-45", "name-105")
        val view = toGlobalViolationTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalViolationTierViews(mutableListOf<GlobalViolationTier>(item, item)).size, "views")
    }
}
