package com.acme.transport.linehaul.capacity.capacityallocation

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedCapacityManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedCapacityManifest("code-980", "name-192")
        val view = toBatchedCapacityManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedCapacityManifestViews(mutableListOf<BatchedCapacityManifest>(item, item)).size, "views")
    }
}
