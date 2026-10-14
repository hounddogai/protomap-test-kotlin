package com.acme.transport.safety.scorecard.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardBundle("code-288", "label-205")
        val view = toScorecardBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardBundleViews(mutableListOf<ScorecardBundle>(item, item)).size, "views")
    }
}
