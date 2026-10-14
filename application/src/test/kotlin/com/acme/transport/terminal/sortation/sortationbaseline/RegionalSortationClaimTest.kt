package com.acme.transport.terminal.sortation.sortationbaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalSortationClaimTest {
    @Test
    fun `starts in draft`() {
        val item = newRegionalSortationClaim("code-169", "name-666")
        assertEquals(RegionalSortationClaimStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRegionalSortationClaim("code-169", "name-666")
        try {
            item.moveTo(RegionalSortationClaimStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalSortationClaimStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRegionalSortationClaim("code-169", "name-666")
        item.status = RegionalSortationClaimStatus.CLOSED
        assertFails("move") { item.moveTo(RegionalSortationClaimStatus.DRAFT) }
    }
}
