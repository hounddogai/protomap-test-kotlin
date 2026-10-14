package com.acme.transport.lastmile.attempt.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptTargetTest {
    @Test
    fun `starts in created`() {
        val item = newAttemptTarget("code-658", "title-195")
        assertEquals(AttemptTargetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newAttemptTarget("code-658", "title-195")
        try {
            item.moveTo(AttemptTargetStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptTargetStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAttemptTarget("code-658", "title-195")
        item.status = AttemptTargetStatus.RETIRED
        assertFails("move") { item.moveTo(AttemptTargetStatus.CREATED) }
    }
}
