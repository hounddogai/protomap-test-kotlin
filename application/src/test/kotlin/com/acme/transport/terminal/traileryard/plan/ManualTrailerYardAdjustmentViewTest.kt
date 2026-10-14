package com.acme.transport.terminal.traileryard.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualTrailerYardAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualTrailerYardAdjustment("code-180", "name-508")
        val view = toManualTrailerYardAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualTrailerYardAdjustmentViews(mutableListOf<ManualTrailerYardAdjustment>(item, item)).size, "views")
    }
}
