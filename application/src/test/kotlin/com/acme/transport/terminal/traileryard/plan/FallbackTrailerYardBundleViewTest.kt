package com.acme.transport.terminal.traileryard.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackTrailerYardBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackTrailerYardBundle("code-11", "name-153")
        val view = toFallbackTrailerYardBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackTrailerYardBundleViews(mutableListOf<FallbackTrailerYardBundle>(item, item)).size, "views")
    }
}
