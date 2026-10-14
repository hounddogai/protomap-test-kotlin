package com.acme.transport.crewing.dutyperiod.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedDutyPeriodExceptionTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedDutyPeriodException("code-921", "name-745")
        assertEquals(WeightedDutyPeriodExceptionStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newWeightedDutyPeriodException("code-921", "name-745")
        try {
            item.moveTo(WeightedDutyPeriodExceptionStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedDutyPeriodExceptionStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedDutyPeriodException("code-921", "name-745")
        item.status = WeightedDutyPeriodExceptionStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedDutyPeriodExceptionStatus.PLANNED) }
    }
}
