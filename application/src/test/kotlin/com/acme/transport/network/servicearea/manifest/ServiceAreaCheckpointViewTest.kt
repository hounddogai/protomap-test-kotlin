package com.acme.transport.network.servicearea.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaCheckpoint("code-920", "label-834")
        val view = toServiceAreaCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaCheckpointViews(mutableListOf<ServiceAreaCheckpoint>(item, item)).size, "views")
    }
}
