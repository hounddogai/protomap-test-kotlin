package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptRollupTest {
    @Test
    fun `starts in draft`() {
        val item = newAttemptRollup("code-858", "title-835")
        assertEquals(AttemptRollupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newAttemptRollup("code-858", "title-835")
        try {
            item.moveTo(AttemptRollupStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptRollupStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAttemptRollup("code-858", "title-835")
        item.status = AttemptRollupStatus.CLOSED
        assertFails("move") { item.moveTo(AttemptRollupStatus.DRAFT) }
    }
}
