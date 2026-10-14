package com.acme.transport.terminal.traileryard.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerYardBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerYardBundle("code-271", "name-541")
        val view = toTrailerYardBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerYardBundleViews(mutableListOf<TrailerYardBundle>(item, item)).size, "views")
    }
}
