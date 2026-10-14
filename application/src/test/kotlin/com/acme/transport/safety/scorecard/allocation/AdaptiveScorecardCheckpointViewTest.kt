package com.acme.transport.safety.scorecard.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveScorecardCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveScorecardCheckpoint("code-639", "title-667")
        val view = toAdaptiveScorecardCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveScorecardCheckpointViews(mutableListOf<AdaptiveScorecardCheckpoint>(item, item)).size, "views")
    }
}
