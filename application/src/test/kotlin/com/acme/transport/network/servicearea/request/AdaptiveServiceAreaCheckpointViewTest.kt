package com.acme.transport.network.servicearea.request

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveServiceAreaCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveServiceAreaCheckpoint("code-907", "title-690")
        val view = toAdaptiveServiceAreaCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveServiceAreaCheckpointViews(mutableListOf<AdaptiveServiceAreaCheckpoint>(item, item)).size, "views")
    }
}
