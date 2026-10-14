package com.acme.transport.safety.scorecard.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardManifest("code-777", "title-618")
        val view = toScorecardManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardManifestViews(mutableListOf<ScorecardManifest>(item, item)).size, "views")
    }
}
