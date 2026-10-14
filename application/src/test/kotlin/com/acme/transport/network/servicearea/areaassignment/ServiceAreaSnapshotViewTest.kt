package com.acme.transport.network.servicearea.areaassignment

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaSnapshot("code-440", "title-54")
        val view = toServiceAreaSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaSnapshotViews(mutableListOf<ServiceAreaSnapshot>(item, item)).size, "views")
    }
}
