package com.acme.transport.linehaul.trailer.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerTemplate("code-110", "title-971")
        val view = toTrailerTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerTemplateViews(mutableListOf<TrailerTemplate>(item, item)).size, "views")
    }
}
