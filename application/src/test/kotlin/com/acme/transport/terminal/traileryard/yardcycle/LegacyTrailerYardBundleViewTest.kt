package com.acme.transport.terminal.traileryard.yardcycle

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyTrailerYardBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyTrailerYardBundle("code-102", "name-318")
        val view = toLegacyTrailerYardBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyTrailerYardBundleViews(mutableListOf<LegacyTrailerYardBundle>(item, item)).size, "views")
    }
}
