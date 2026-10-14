package com.acme.transport.network.hub.primaryhubsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedHubTierTest {
    @Test
    fun `starts in planned`() {
        val item = newBatchedHubTier("code-292", "title-195")
        assertEquals(BatchedHubTierStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newBatchedHubTier("code-292", "title-195")
        try {
            item.moveTo(BatchedHubTierStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedHubTierStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBatchedHubTier("code-292", "title-195")
        item.status = BatchedHubTierStatus.CANCELLED
        assertFails("move") { item.moveTo(BatchedHubTierStatus.PLANNED) }
    }
}
