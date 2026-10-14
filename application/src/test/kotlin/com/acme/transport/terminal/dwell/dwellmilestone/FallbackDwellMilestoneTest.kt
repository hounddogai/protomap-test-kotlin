package com.acme.transport.terminal.dwell.dwellmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDwellMilestoneTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackDwellMilestone("code-604", "name-247")
        assertEquals(FallbackDwellMilestoneStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newFallbackDwellMilestone("code-604", "name-247")
        try {
            item.moveTo(FallbackDwellMilestoneStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDwellMilestoneStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackDwellMilestone("code-604", "name-247")
        item.status = FallbackDwellMilestoneStatus.FAILED
        assertFails("move") { item.moveTo(FallbackDwellMilestoneStatus.NEW) }
    }
}
