package com.acme.transport.lastmile.locker.lockertarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedLockerClaimTest {
    @Test
    fun `starts in created`() {
        val item = newBatchedLockerClaim("code-543", "name-245")
        assertEquals(BatchedLockerClaimStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newBatchedLockerClaim("code-543", "name-245")
        try {
            item.moveTo(BatchedLockerClaimStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedLockerClaimStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBatchedLockerClaim("code-543", "name-245")
        item.status = BatchedLockerClaimStatus.RETIRED
        assertFails("move") { item.moveTo(BatchedLockerClaimStatus.CREATED) }
    }
}
