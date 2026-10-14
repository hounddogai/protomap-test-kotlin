package com.acme.transport.crewing.dutyperiod.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDutyPeriodEstimateTest {
    @Test
    fun `starts in created`() {
        val item = newManualDutyPeriodEstimate("code-82", "name-617")
        assertEquals(ManualDutyPeriodEstimateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newManualDutyPeriodEstimate("code-82", "name-617")
        try {
            item.moveTo(ManualDutyPeriodEstimateStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDutyPeriodEstimateStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualDutyPeriodEstimate("code-82", "name-617")
        item.status = ManualDutyPeriodEstimateStatus.RETIRED
        assertFails("move") { item.moveTo(ManualDutyPeriodEstimateStatus.CREATED) }
    }
}
