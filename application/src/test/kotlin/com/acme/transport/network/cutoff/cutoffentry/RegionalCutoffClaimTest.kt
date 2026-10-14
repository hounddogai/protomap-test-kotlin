package com.acme.transport.network.cutoff.cutoffentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCutoffClaimTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalCutoffClaim("code-515", "title-29")
        assertEquals(RegionalCutoffClaimStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRegionalCutoffClaim("code-515", "title-29")
        try {
            item.moveTo(RegionalCutoffClaimStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCutoffClaimStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalCutoffClaim("code-515", "title-29")
        item.status = RegionalCutoffClaimStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalCutoffClaimStatus.CREATED) }
    }
}
