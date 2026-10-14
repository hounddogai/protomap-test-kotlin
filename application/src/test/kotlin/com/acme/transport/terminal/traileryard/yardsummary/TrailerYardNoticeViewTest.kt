package com.acme.transport.terminal.traileryard.yardsummary

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerYardNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerYardNotice("code-499", "title-193")
        val view = toTrailerYardNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerYardNoticeViews(mutableListOf<TrailerYardNotice>(item, item)).size, "views")
    }
}
