package com.acme.transport.crewing.dutyperiod.periodassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedDutyPeriodClaimTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedDutyPeriodClaim("code-378", "title-621")
        assertEquals(WeightedDutyPeriodClaimStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newWeightedDutyPeriodClaim("code-378", "title-621")
        try {
            item.moveTo(WeightedDutyPeriodClaimStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedDutyPeriodClaimStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedDutyPeriodClaim("code-378", "title-621")
        item.status = WeightedDutyPeriodClaimStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedDutyPeriodClaimStatus.PLANNED) }
    }
}
