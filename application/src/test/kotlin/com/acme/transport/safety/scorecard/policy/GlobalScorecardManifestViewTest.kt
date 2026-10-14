package com.acme.transport.safety.scorecard.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalScorecardManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalScorecardManifest("code-667", "name-348")
        val view = toGlobalScorecardManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalScorecardManifestViews(mutableListOf<GlobalScorecardManifest>(item, item)).size, "views")
    }
}
