package com.acme.transport.terminal.crossdock.dockproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryCrossDockMilestoneTest {
    @Test
    fun `starts in proposed`() {
        val item = newPrimaryCrossDockMilestone("code-762", "name-317")
        assertEquals(PrimaryCrossDockMilestoneStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newPrimaryCrossDockMilestone("code-762", "name-317")
        try {
            item.moveTo(PrimaryCrossDockMilestoneStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryCrossDockMilestoneStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPrimaryCrossDockMilestone("code-762", "name-317")
        item.status = PrimaryCrossDockMilestoneStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PrimaryCrossDockMilestoneStatus.PROPOSED) }
    }
}
