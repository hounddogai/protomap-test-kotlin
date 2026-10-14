package com.acme.transport.safety.violation.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationMilestoneTest {
    @Test
    fun `starts in created`() {
        val item = newViolationMilestone("code-488", "label-912")
        assertEquals(ViolationMilestoneStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newViolationMilestone("code-488", "label-912")
        try {
            item.moveTo(ViolationMilestoneStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationMilestoneStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newViolationMilestone("code-488", "label-912")
        item.status = ViolationMilestoneStatus.RETIRED
        assertFails("move") { item.moveTo(ViolationMilestoneStatus.CREATED) }
    }
}
