package com.acme.transport.crewing.dutyperiod.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedDutyPeriodAllocationTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedDutyPeriodAllocation("code-225", "label-478")
        assertEquals(WeightedDutyPeriodAllocationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newWeightedDutyPeriodAllocation("code-225", "label-478")
        try {
            item.moveTo(WeightedDutyPeriodAllocationStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedDutyPeriodAllocationStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedDutyPeriodAllocation("code-225", "label-478")
        item.status = WeightedDutyPeriodAllocationStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedDutyPeriodAllocationStatus.REQUESTED) }
    }
}
