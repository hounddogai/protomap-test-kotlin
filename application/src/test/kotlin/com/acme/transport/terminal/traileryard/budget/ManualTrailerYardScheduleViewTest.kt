package com.acme.transport.terminal.traileryard.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualTrailerYardScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualTrailerYardSchedule("code-986", "label-60")
        val view = toManualTrailerYardScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualTrailerYardScheduleViews(mutableListOf<ManualTrailerYardSchedule>(item, item)).size, "views")
    }
}
