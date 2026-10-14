package com.acme.transport.network.hub.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedHubClaimTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedHubClaim("code-389", "title-544")
        assertEquals(BatchedHubClaimStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newBatchedHubClaim("code-389", "title-544")
        try {
            item.moveTo(BatchedHubClaimStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedHubClaimStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedHubClaim("code-389", "title-544")
        item.status = BatchedHubClaimStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedHubClaimStatus.OPEN) }
    }
}
