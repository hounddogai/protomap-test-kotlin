package com.acme.transport.fleetops.defect.defectcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDefectMilestoneTest {
    @Test
    fun `starts in created`() {
        val item = newStrictDefectMilestone("code-821", "label-62")
        assertEquals(StrictDefectMilestoneStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newStrictDefectMilestone("code-821", "label-62")
        try {
            item.moveTo(StrictDefectMilestoneStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDefectMilestoneStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newStrictDefectMilestone("code-821", "label-62")
        item.status = StrictDefectMilestoneStatus.RETIRED
        assertFails("move") { item.moveTo(StrictDefectMilestoneStatus.CREATED) }
    }
}
