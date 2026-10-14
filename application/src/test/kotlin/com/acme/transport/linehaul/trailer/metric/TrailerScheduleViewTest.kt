package com.acme.transport.linehaul.trailer.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerSchedule("code-483", "name-776")
        val view = toTrailerScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerScheduleViews(mutableListOf<TrailerSchedule>(item, item)).size, "views")
    }
}
