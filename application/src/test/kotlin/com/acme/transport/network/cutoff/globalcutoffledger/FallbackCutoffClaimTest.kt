package com.acme.transport.network.cutoff.globalcutoffledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackCutoffClaimTest {
    @Test
    fun `starts in created`() {
        val item = newFallbackCutoffClaim("code-582", "label-801")
        assertEquals(FallbackCutoffClaimStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newFallbackCutoffClaim("code-582", "label-801")
        try {
            item.moveTo(FallbackCutoffClaimStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackCutoffClaimStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFallbackCutoffClaim("code-582", "label-801")
        item.status = FallbackCutoffClaimStatus.RETIRED
        assertFails("move") { item.moveTo(FallbackCutoffClaimStatus.CREATED) }
    }
}
