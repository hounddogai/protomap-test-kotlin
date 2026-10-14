package com.acme.transport.lastmile.attempt.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyAttemptCycleTest {
    @Test
    fun `starts in draft`() {
        val item = newLegacyAttemptCycle("code-326", "title-228")
        assertEquals(LegacyAttemptCycleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newLegacyAttemptCycle("code-326", "title-228")
        try {
            item.moveTo(LegacyAttemptCycleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyAttemptCycleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLegacyAttemptCycle("code-326", "title-228")
        item.status = LegacyAttemptCycleStatus.CLOSED
        assertFails("move") { item.moveTo(LegacyAttemptCycleStatus.DRAFT) }
    }
}
