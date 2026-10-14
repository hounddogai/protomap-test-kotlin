package com.acme.transport.crewing.swap.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapClaimTest {
    @Test
    fun `starts in new`() {
        val item = newSwapClaim("code-376", "title-42")
        assertEquals(SwapClaimStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newSwapClaim("code-376", "title-42")
        try {
            item.moveTo(SwapClaimStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapClaimStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSwapClaim("code-376", "title-42")
        item.status = SwapClaimStatus.FAILED
        assertFails("move") { item.moveTo(SwapClaimStatus.NEW) }
    }
}
