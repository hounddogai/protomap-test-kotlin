package com.acme.transport.terminal.traileryard.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedTrailerYardSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedTrailerYardSnapshot("code-501", "title-483")
        val view = toWeightedTrailerYardSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedTrailerYardSnapshotViews(mutableListOf<WeightedTrailerYardSnapshot>(item, item)).size, "views")
    }
}
