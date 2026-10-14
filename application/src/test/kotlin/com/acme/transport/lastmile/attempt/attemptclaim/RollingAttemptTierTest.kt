package com.acme.transport.lastmile.attempt.attemptclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingAttemptTierTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingAttemptTier("code-852", "label-294")
        assertEquals(RollingAttemptTierStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newRollingAttemptTier("code-852", "label-294")
        try {
            item.moveTo(RollingAttemptTierStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingAttemptTierStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingAttemptTier("code-852", "label-294")
        item.status = RollingAttemptTierStatus.CLOSED
        assertFails("move") { item.moveTo(RollingAttemptTierStatus.DRAFT) }
    }
}
