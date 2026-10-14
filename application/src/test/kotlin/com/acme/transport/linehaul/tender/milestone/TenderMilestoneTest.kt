package com.acme.transport.linehaul.tender.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderMilestoneTest {
    @Test
    fun `starts in new`() {
        val item = newTenderMilestone("code-298", "label-34")
        assertEquals(TenderMilestoneStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newTenderMilestone("code-298", "label-34")
        try {
            item.moveTo(TenderMilestoneStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderMilestoneStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTenderMilestone("code-298", "label-34")
        item.status = TenderMilestoneStatus.FAILED
        assertFails("move") { item.moveTo(TenderMilestoneStatus.NEW) }
    }
}
