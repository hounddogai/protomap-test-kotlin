package com.acme.transport.crewing.dutyperiod.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedDutyPeriodForecastTest {
    @Test
    fun `starts in created`() {
        val item = newBatchedDutyPeriodForecast("code-679", "name-882")
        assertEquals(BatchedDutyPeriodForecastStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newBatchedDutyPeriodForecast("code-679", "name-882")
        try {
            item.moveTo(BatchedDutyPeriodForecastStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedDutyPeriodForecastStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBatchedDutyPeriodForecast("code-679", "name-882")
        item.status = BatchedDutyPeriodForecastStatus.RETIRED
        assertFails("move") { item.moveTo(BatchedDutyPeriodForecastStatus.CREATED) }
    }
}
