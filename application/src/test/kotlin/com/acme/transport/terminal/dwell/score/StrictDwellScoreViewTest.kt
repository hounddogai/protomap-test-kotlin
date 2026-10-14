package com.acme.transport.terminal.dwell.score

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictDwellScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictDwellScore("code-31", "name-900")
        val view = toStrictDwellScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictDwellScoreViews(mutableListOf<StrictDwellScore>(item, item)).size, "views")
    }
}
