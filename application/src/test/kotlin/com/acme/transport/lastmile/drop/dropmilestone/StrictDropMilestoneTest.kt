package com.acme.transport.lastmile.drop.dropmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDropMilestoneTest {
    @Test
    fun `starts in new`() {
        val item = newStrictDropMilestone("code-776", "name-332")
        assertEquals(StrictDropMilestoneStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newStrictDropMilestone("code-776", "name-332")
        try {
            item.moveTo(StrictDropMilestoneStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDropMilestoneStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newStrictDropMilestone("code-776", "name-332")
        item.status = StrictDropMilestoneStatus.FAILED
        assertFails("move") { item.moveTo(StrictDropMilestoneStatus.NEW) }
    }
}
