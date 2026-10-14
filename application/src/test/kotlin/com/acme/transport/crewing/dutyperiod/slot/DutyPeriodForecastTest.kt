package com.acme.transport.crewing.dutyperiod.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodForecastTest {
    @Test
    fun `starts in created`() {
        val item = newDutyPeriodForecast("code-531", "name-210")
        assertEquals(DutyPeriodForecastStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newDutyPeriodForecast("code-531", "name-210")
        try {
            item.moveTo(DutyPeriodForecastStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodForecastStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDutyPeriodForecast("code-531", "name-210")
        item.status = DutyPeriodForecastStatus.RETIRED
        assertFails("move") { item.moveTo(DutyPeriodForecastStatus.CREATED) }
    }
}
