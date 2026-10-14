package com.acme.transport.customs.inspection.inspectiontarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedInspectionClaimTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedInspectionClaim("code-244", "name-284")
        assertEquals(BatchedInspectionClaimStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newBatchedInspectionClaim("code-244", "name-284")
        try {
            item.moveTo(BatchedInspectionClaimStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedInspectionClaimStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedInspectionClaim("code-244", "name-284")
        item.status = BatchedInspectionClaimStatus.FAILED
        assertFails("move") { item.moveTo(BatchedInspectionClaimStatus.NEW) }
    }
}
