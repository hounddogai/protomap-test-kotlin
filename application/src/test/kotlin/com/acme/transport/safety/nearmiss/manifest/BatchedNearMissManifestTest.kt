package com.acme.transport.safety.nearmiss.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedNearMissManifestTest {
    @Test
    fun `starts in planned`() {
        val item = newBatchedNearMissManifest("code-514", "title-647")
        assertEquals(BatchedNearMissManifestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newBatchedNearMissManifest("code-514", "title-647")
        try {
            item.moveTo(BatchedNearMissManifestStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedNearMissManifestStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBatchedNearMissManifest("code-514", "title-647")
        item.status = BatchedNearMissManifestStatus.CANCELLED
        assertFails("move") { item.moveTo(BatchedNearMissManifestStatus.PLANNED) }
    }
}
