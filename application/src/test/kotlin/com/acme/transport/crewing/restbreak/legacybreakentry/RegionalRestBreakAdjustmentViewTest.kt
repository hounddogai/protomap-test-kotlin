package com.acme.transport.crewing.restbreak.legacybreakentry

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalRestBreakAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalRestBreakAdjustment("code-281", "name-156")
        val view = toRegionalRestBreakAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalRestBreakAdjustmentViews(mutableListOf<RegionalRestBreakAdjustment>(item, item)).size, "views")
    }
}
