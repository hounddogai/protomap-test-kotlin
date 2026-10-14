package com.acme.transport.terminal.traileryard.template

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerYardTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerYardTemplate("code-631", "title-589")
        val view = toTrailerYardTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerYardTemplateViews(mutableListOf<TrailerYardTemplate>(item, item)).size, "views")
    }
}
