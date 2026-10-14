package com.acme.transport.linehaul.trailer.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerDraft("code-844", "name-309")
        val view = toTrailerDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerDraftViews(mutableListOf<TrailerDraft>(item, item)).size, "views")
    }
}
