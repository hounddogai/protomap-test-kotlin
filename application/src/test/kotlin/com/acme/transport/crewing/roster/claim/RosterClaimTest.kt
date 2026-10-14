package com.acme.transport.crewing.roster.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterClaimTest {
    @Test
    fun `starts in open`() {
        val item = newRosterClaim("code-494", "name-31")
        assertEquals(RosterClaimStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newRosterClaim("code-494", "name-31")
        try {
            item.moveTo(RosterClaimStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterClaimStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRosterClaim("code-494", "name-31")
        item.status = RosterClaimStatus.ARCHIVED
        assertFails("move") { item.moveTo(RosterClaimStatus.OPEN) }
    }
}
