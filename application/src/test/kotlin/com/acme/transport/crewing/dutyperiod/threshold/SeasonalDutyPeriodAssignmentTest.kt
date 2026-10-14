package com.acme.transport.crewing.dutyperiod.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDutyPeriodAssignmentTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalDutyPeriodAssignment("code-793", "name-974")
        assertEquals(SeasonalDutyPeriodAssignmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newSeasonalDutyPeriodAssignment("code-793", "name-974")
        try {
            item.moveTo(SeasonalDutyPeriodAssignmentStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDutyPeriodAssignmentStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalDutyPeriodAssignment("code-793", "name-974")
        item.status = SeasonalDutyPeriodAssignmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalDutyPeriodAssignmentStatus.OPEN) }
    }
}
