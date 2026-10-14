package com.acme.transport.lastmile.attempt.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultAttemptClaimTest {
    @Test
    fun `starts in draft`() {
        val item = newDefaultAttemptClaim("code-252", "title-922")
        assertEquals(DefaultAttemptClaimStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newDefaultAttemptClaim("code-252", "title-922")
        try {
            item.moveTo(DefaultAttemptClaimStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultAttemptClaimStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDefaultAttemptClaim("code-252", "title-922")
        item.status = DefaultAttemptClaimStatus.CLOSED
        assertFails("move") { item.moveTo(DefaultAttemptClaimStatus.DRAFT) }
    }
}
