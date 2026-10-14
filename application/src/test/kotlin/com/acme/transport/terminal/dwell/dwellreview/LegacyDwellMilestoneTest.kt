package com.acme.transport.terminal.dwell.dwellreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyDwellMilestoneTest {
    @Test
    fun `starts in created`() {
        val item = newLegacyDwellMilestone("code-448", "label-94")
        assertEquals(LegacyDwellMilestoneStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLegacyDwellMilestone("code-448", "label-94")
        try {
            item.moveTo(LegacyDwellMilestoneStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyDwellMilestoneStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLegacyDwellMilestone("code-448", "label-94")
        item.status = LegacyDwellMilestoneStatus.RETIRED
        assertFails("move") { item.moveTo(LegacyDwellMilestoneStatus.CREATED) }
    }
}
