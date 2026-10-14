package com.acme.transport.crewing.dutyperiod.periodthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedDutyPeriodAdjustmentTest {
    @Test
    fun `starts in planned`() {
        val item = newCappedDutyPeriodAdjustment("code-263", "name-574")
        assertEquals(CappedDutyPeriodAdjustmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newCappedDutyPeriodAdjustment("code-263", "name-574")
        try {
            item.moveTo(CappedDutyPeriodAdjustmentStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedDutyPeriodAdjustmentStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCappedDutyPeriodAdjustment("code-263", "name-574")
        item.status = CappedDutyPeriodAdjustmentStatus.CANCELLED
        assertFails("move") { item.moveTo(CappedDutyPeriodAdjustmentStatus.PLANNED) }
    }
}
