package com.acme.transport.lastmile.attempt.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptMilestoneTest {
    @Test
    fun `starts in created`() {
        val item = newAttemptMilestone("code-950", "title-223")
        assertEquals(AttemptMilestoneStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newAttemptMilestone("code-950", "title-223")
        try {
            item.moveTo(AttemptMilestoneStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptMilestoneStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAttemptMilestone("code-950", "title-223")
        item.status = AttemptMilestoneStatus.RETIRED
        assertFails("move") { item.moveTo(AttemptMilestoneStatus.CREATED) }
    }
}
