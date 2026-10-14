package com.acme.transport.terminal.dwell.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackDwellScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackDwellScore("code-650", "name-640")
        val view = toFallbackDwellScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackDwellScoreViews(mutableListOf<FallbackDwellScore>(item, item)).size, "views")
    }
}
