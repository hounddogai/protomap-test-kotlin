package com.acme.transport.terminal.dwell.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDwellMilestoneTest {
    @Test
    fun `starts in new`() {
        val item = newLenientDwellMilestone("code-158", "label-754")
        assertEquals(LenientDwellMilestoneStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLenientDwellMilestone("code-158", "label-754")
        try {
            item.moveTo(LenientDwellMilestoneStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDwellMilestoneStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLenientDwellMilestone("code-158", "label-754")
        item.status = LenientDwellMilestoneStatus.FAILED
        assertFails("move") { item.moveTo(LenientDwellMilestoneStatus.NEW) }
    }
}
