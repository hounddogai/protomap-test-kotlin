package com.acme.transport.freightpay.dispute.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeMilestoneTest {
    @Test
    fun `starts in open`() {
        val item = newDisputeMilestone("code-429", "label-173")
        assertEquals(DisputeMilestoneStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newDisputeMilestone("code-429", "label-173")
        try {
            item.moveTo(DisputeMilestoneStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeMilestoneStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDisputeMilestone("code-429", "label-173")
        item.status = DisputeMilestoneStatus.ARCHIVED
        assertFails("move") { item.moveTo(DisputeMilestoneStatus.OPEN) }
    }
}
