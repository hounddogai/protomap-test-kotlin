package com.acme.transport.lastmile.redelivery.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryMilestoneTest {
    @Test
    fun `starts in new`() {
        val item = newRedeliveryMilestone("code-739", "name-982")
        assertEquals(RedeliveryMilestoneStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRedeliveryMilestone("code-739", "name-982")
        try {
            item.moveTo(RedeliveryMilestoneStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryMilestoneStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRedeliveryMilestone("code-739", "name-982")
        item.status = RedeliveryMilestoneStatus.FAILED
        assertFails("move") { item.moveTo(RedeliveryMilestoneStatus.NEW) }
    }
}
