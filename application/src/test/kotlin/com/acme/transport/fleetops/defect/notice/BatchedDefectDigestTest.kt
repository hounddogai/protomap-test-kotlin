package com.acme.transport.fleetops.defect.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedDefectDigestTest {
    @Test
    fun `starts in created`() {
        val item = newBatchedDefectDigest("code-218", "label-267")
        assertEquals(BatchedDefectDigestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newBatchedDefectDigest("code-218", "label-267")
        try {
            item.moveTo(BatchedDefectDigestStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedDefectDigestStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBatchedDefectDigest("code-218", "label-267")
        item.status = BatchedDefectDigestStatus.RETIRED
        assertFails("move") { item.moveTo(BatchedDefectDigestStatus.CREATED) }
    }
}
