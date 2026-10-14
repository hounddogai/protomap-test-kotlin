package com.acme.transport.lastmile.attempt.attemptpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackAttemptPolicyTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackAttemptPolicy("code-545", "name-565")
        assertEquals(FallbackAttemptPolicyStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newFallbackAttemptPolicy("code-545", "name-565")
        try {
            item.moveTo(FallbackAttemptPolicyStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackAttemptPolicyStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackAttemptPolicy("code-545", "name-565")
        item.status = FallbackAttemptPolicyStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackAttemptPolicyStatus.OPEN) }
    }
}
