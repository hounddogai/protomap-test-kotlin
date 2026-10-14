package com.acme.transport.crewing.qualification.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalQualificationMilestoneTest {
    @Test
    fun `starts in planned`() {
        val item = newSeasonalQualificationMilestone("code-705", "label-971")
        assertEquals(SeasonalQualificationMilestoneStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newSeasonalQualificationMilestone("code-705", "label-971")
        try {
            item.moveTo(SeasonalQualificationMilestoneStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalQualificationMilestoneStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSeasonalQualificationMilestone("code-705", "label-971")
        item.status = SeasonalQualificationMilestoneStatus.CANCELLED
        assertFails("move") { item.moveTo(SeasonalQualificationMilestoneStatus.PLANNED) }
    }
}
