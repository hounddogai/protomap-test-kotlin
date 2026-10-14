package com.acme.transport.safety.violation.violationslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictViolationMilestoneTest {
    @Test
    fun `starts in open`() {
        val item = newStrictViolationMilestone("code-383", "name-96")
        assertEquals(StrictViolationMilestoneStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newStrictViolationMilestone("code-383", "name-96")
        try {
            item.moveTo(StrictViolationMilestoneStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictViolationMilestoneStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictViolationMilestone("code-383", "name-96")
        item.status = StrictViolationMilestoneStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictViolationMilestoneStatus.OPEN) }
    }
}
