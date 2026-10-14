package com.acme.transport.crewing.roster.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultRosterClaimTest {
    @Test
    fun `starts in planned`() {
        val item = newDefaultRosterClaim("code-433", "label-830")
        assertEquals(DefaultRosterClaimStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newDefaultRosterClaim("code-433", "label-830")
        try {
            item.moveTo(DefaultRosterClaimStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultRosterClaimStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDefaultRosterClaim("code-433", "label-830")
        item.status = DefaultRosterClaimStatus.CANCELLED
        assertFails("move") { item.moveTo(DefaultRosterClaimStatus.PLANNED) }
    }
}
