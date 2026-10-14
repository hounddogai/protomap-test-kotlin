package com.acme.transport.crewing.dutyperiod.periodreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingDutyPeriodForecastTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingDutyPeriodForecast("code-967", "label-199")
        assertEquals(RollingDutyPeriodForecastStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRollingDutyPeriodForecast("code-967", "label-199")
        try {
            item.moveTo(RollingDutyPeriodForecastStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingDutyPeriodForecastStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingDutyPeriodForecast("code-967", "label-199")
        item.status = RollingDutyPeriodForecastStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingDutyPeriodForecastStatus.PROPOSED) }
    }
}
