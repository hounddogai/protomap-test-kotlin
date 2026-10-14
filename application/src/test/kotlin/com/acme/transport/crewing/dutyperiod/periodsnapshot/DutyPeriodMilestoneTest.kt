package com.acme.transport.crewing.dutyperiod.periodsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodMilestoneTest {
    @Test
    fun `starts in open`() {
        val item = newDutyPeriodMilestone("code-460", "title-860")
        assertEquals(DutyPeriodMilestoneStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newDutyPeriodMilestone("code-460", "title-860")
        try {
            item.moveTo(DutyPeriodMilestoneStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodMilestoneStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDutyPeriodMilestone("code-460", "title-860")
        item.status = DutyPeriodMilestoneStatus.ARCHIVED
        assertFails("move") { item.moveTo(DutyPeriodMilestoneStatus.OPEN) }
    }
}
