package com.acme.transport.network.servicearea.areaexception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedServiceAreaSnapshotTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedServiceAreaSnapshot("code-34", "name-504")
        assertEquals(BatchedServiceAreaSnapshotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newBatchedServiceAreaSnapshot("code-34", "name-504")
        try {
            item.moveTo(BatchedServiceAreaSnapshotStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedServiceAreaSnapshotStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedServiceAreaSnapshot("code-34", "name-504")
        item.status = BatchedServiceAreaSnapshotStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedServiceAreaSnapshotStatus.OPEN) }
    }
}
