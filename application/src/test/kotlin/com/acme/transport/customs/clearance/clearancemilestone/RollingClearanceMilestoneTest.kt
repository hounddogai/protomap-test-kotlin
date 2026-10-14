package com.acme.transport.customs.clearance.clearancemilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingClearanceMilestoneTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingClearanceMilestone("code-390", "label-34")
        assertEquals(RollingClearanceMilestoneStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRollingClearanceMilestone("code-390", "label-34")
        try {
            item.moveTo(RollingClearanceMilestoneStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingClearanceMilestoneStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingClearanceMilestone("code-390", "label-34")
        item.status = RollingClearanceMilestoneStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingClearanceMilestoneStatus.PROPOSED) }
    }
}
