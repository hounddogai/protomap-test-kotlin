package com.acme.transport.network.transittime.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyTransitTimeCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyTransitTimeCheckpoint("code-578", "label-773")
        val view = toLegacyTransitTimeCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyTransitTimeCheckpointViews(mutableListOf<LegacyTransitTimeCheckpoint>(item, item)).size, "views")
    }
}
