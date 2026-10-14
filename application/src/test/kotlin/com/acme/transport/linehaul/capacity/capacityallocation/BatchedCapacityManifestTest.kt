package com.acme.transport.linehaul.capacity.capacityallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedCapacityManifestTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedCapacityManifest("code-980", "name-192")
        assertEquals(BatchedCapacityManifestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newBatchedCapacityManifest("code-980", "name-192")
        try {
            item.moveTo(BatchedCapacityManifestStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedCapacityManifestStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedCapacityManifest("code-980", "name-192")
        item.status = BatchedCapacityManifestStatus.FAILED
        assertFails("move") { item.moveTo(BatchedCapacityManifestStatus.NEW) }
    }
}
