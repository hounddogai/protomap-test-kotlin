package com.acme.transport.terminal.traileryard.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerYardDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerYardDraft("code-196", "title-165")
        val view = toTrailerYardDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerYardDraftViews(mutableListOf<TrailerYardDraft>(item, item)).size, "views")
    }
}
