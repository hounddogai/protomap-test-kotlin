package com.acme.transport.network.cutoff.cutoffclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalCutoffClaimTest {
    @Test
    fun `starts in draft`() {
        val item = newSeasonalCutoffClaim("code-917", "name-102")
        assertEquals(SeasonalCutoffClaimStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newSeasonalCutoffClaim("code-917", "name-102")
        try {
            item.moveTo(SeasonalCutoffClaimStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalCutoffClaimStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSeasonalCutoffClaim("code-917", "name-102")
        item.status = SeasonalCutoffClaimStatus.CLOSED
        assertFails("move") { item.moveTo(SeasonalCutoffClaimStatus.DRAFT) }
    }
}
