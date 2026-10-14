package com.acme.transport.crewing.dutyperiod.periodthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyDutyPeriodThresholdTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyDutyPeriodThreshold("code-516", "label-815")
        assertEquals(LegacyDutyPeriodThresholdStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newLegacyDutyPeriodThreshold("code-516", "label-815")
        try {
            item.moveTo(LegacyDutyPeriodThresholdStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyDutyPeriodThresholdStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyDutyPeriodThreshold("code-516", "label-815")
        item.status = LegacyDutyPeriodThresholdStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyDutyPeriodThresholdStatus.PLANNED) }
    }
}
