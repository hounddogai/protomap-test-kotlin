package com.acme.transport.linehaul.laneplan.plancheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackLanePlanCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackLanePlanCheckpoint("code-197", "title-550")
        val view = toFallbackLanePlanCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackLanePlanCheckpointViews(mutableListOf<FallbackLanePlanCheckpoint>(item, item)).size, "views")
    }
}
