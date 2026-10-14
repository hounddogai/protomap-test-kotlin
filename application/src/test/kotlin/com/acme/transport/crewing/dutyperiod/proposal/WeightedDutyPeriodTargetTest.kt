package com.acme.transport.crewing.dutyperiod.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedDutyPeriodTargetTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedDutyPeriodTarget("code-82", "name-179")
        assertEquals(WeightedDutyPeriodTargetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newWeightedDutyPeriodTarget("code-82", "name-179")
        try {
            item.moveTo(WeightedDutyPeriodTargetStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedDutyPeriodTargetStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedDutyPeriodTarget("code-82", "name-179")
        item.status = WeightedDutyPeriodTargetStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedDutyPeriodTargetStatus.PLANNED) }
    }
}
