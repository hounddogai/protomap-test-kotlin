package com.acme.transport.linehaul.trailer.template

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerWindow("code-847", "title-990")
        val view = toTrailerWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerWindowViews(mutableListOf<TrailerWindow>(item, item)).size, "views")
    }
}
