package com.acme.transport.crewing.restbreak.legacybreakentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingRestBreakMilestoneTest {
    @Test
    fun `starts in requested`() {
        val item = newRollingRestBreakMilestone("code-990", "title-881")
        assertEquals(RollingRestBreakMilestoneStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRollingRestBreakMilestone("code-990", "title-881")
        try {
            item.moveTo(RollingRestBreakMilestoneStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingRestBreakMilestoneStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRollingRestBreakMilestone("code-990", "title-881")
        item.status = RollingRestBreakMilestoneStatus.EXPIRED
        assertFails("move") { item.moveTo(RollingRestBreakMilestoneStatus.REQUESTED) }
    }
}
