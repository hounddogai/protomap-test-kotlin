package com.acme.transport.freightpay.dispute.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDisputeRollupTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackDisputeRollup("code-590", "title-762")
        assertEquals(FallbackDisputeRollupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newFallbackDisputeRollup("code-590", "title-762")
        try {
            item.moveTo(FallbackDisputeRollupStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDisputeRollupStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackDisputeRollup("code-590", "title-762")
        item.status = FallbackDisputeRollupStatus.FAILED
        assertFails("move") { item.moveTo(FallbackDisputeRollupStatus.NEW) }
    }
}
