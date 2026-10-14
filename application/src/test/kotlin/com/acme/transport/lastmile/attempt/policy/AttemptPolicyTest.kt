package com.acme.transport.lastmile.attempt.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptPolicyTest {
    @Test
    fun `starts in requested`() {
        val item = newAttemptPolicy("code-835", "name-797")
        assertEquals(AttemptPolicyStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newAttemptPolicy("code-835", "name-797")
        try {
            item.moveTo(AttemptPolicyStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptPolicyStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAttemptPolicy("code-835", "name-797")
        item.status = AttemptPolicyStatus.EXPIRED
        assertFails("move") { item.moveTo(AttemptPolicyStatus.REQUESTED) }
    }
}
