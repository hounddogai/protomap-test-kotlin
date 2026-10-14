package com.acme.transport.lastmile.attempt.defaultattemptpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyAttemptPolicyTest {
    @Test
    fun `starts in draft`() {
        val item = newLegacyAttemptPolicy("code-385", "label-460")
        assertEquals(LegacyAttemptPolicyStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newLegacyAttemptPolicy("code-385", "label-460")
        try {
            item.moveTo(LegacyAttemptPolicyStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyAttemptPolicyStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLegacyAttemptPolicy("code-385", "label-460")
        item.status = LegacyAttemptPolicyStatus.CLOSED
        assertFails("move") { item.moveTo(LegacyAttemptPolicyStatus.DRAFT) }
    }
}
