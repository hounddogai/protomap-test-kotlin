package com.acme.transport.crewing.dutyperiod.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDutyPeriodEstimateTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalDutyPeriodEstimate("code-389", "title-810")
        assertEquals(RegionalDutyPeriodEstimateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRegionalDutyPeriodEstimate("code-389", "title-810")
        try {
            item.moveTo(RegionalDutyPeriodEstimateStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDutyPeriodEstimateStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalDutyPeriodEstimate("code-389", "title-810")
        item.status = RegionalDutyPeriodEstimateStatus.FAILED
        assertFails("move") { item.moveTo(RegionalDutyPeriodEstimateStatus.NEW) }
    }
}
