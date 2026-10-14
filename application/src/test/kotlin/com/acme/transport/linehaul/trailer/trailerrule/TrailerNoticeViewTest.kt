package com.acme.transport.linehaul.trailer.trailerrule

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerNotice("code-756", "name-317")
        val view = toTrailerNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerNoticeViews(mutableListOf<TrailerNotice>(item, item)).size, "views")
    }
}
