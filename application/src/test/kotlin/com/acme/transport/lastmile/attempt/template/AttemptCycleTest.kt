package com.acme.transport.lastmile.attempt.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptCycleTest {
    @Test
    fun `starts in open`() {
        val item = newAttemptCycle("code-766", "name-125")
        assertEquals(AttemptCycleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newAttemptCycle("code-766", "name-125")
        try {
            item.moveTo(AttemptCycleStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptCycleStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAttemptCycle("code-766", "name-125")
        item.status = AttemptCycleStatus.ARCHIVED
        assertFails("move") { item.moveTo(AttemptCycleStatus.OPEN) }
    }
}
