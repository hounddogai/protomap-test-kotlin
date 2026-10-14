package com.acme.transport.crewing.dutyperiod.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledDutyPeriodEstimateTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledDutyPeriodEstimate("code-593", "title-768")
        assertEquals(ScheduledDutyPeriodEstimateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newScheduledDutyPeriodEstimate("code-593", "title-768")
        try {
            item.moveTo(ScheduledDutyPeriodEstimateStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledDutyPeriodEstimateStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledDutyPeriodEstimate("code-593", "title-768")
        item.status = ScheduledDutyPeriodEstimateStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledDutyPeriodEstimateStatus.DRAFT) }
    }
}
