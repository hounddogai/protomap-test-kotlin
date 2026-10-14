package com.acme.transport.fleetops.defect.defectagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedDefectClaimTest {
    @Test
    fun `starts in planned`() {
        val item = newBatchedDefectClaim("code-649", "label-395")
        assertEquals(BatchedDefectClaimStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newBatchedDefectClaim("code-649", "label-395")
        try {
            item.moveTo(BatchedDefectClaimStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedDefectClaimStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBatchedDefectClaim("code-649", "label-395")
        item.status = BatchedDefectClaimStatus.CANCELLED
        assertFails("move") { item.moveTo(BatchedDefectClaimStatus.PLANNED) }
    }
}
