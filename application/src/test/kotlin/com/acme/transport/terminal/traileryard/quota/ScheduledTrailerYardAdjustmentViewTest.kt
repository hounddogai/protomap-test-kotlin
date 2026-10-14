package com.acme.transport.terminal.traileryard.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledTrailerYardAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledTrailerYardAdjustment("code-530", "name-221")
        val view = toScheduledTrailerYardAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledTrailerYardAdjustmentViews(mutableListOf<ScheduledTrailerYardAdjustment>(item, item)).size, "views")
    }
}
