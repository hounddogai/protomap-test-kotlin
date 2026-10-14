package com.acme.transport.crewing.dutyperiod.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDutyPeriodBatchTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalDutyPeriodBatch("code-299", "label-552")
        assertEquals(SeasonalDutyPeriodBatchStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newSeasonalDutyPeriodBatch("code-299", "label-552")
        try {
            item.moveTo(SeasonalDutyPeriodBatchStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDutyPeriodBatchStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalDutyPeriodBatch("code-299", "label-552")
        item.status = SeasonalDutyPeriodBatchStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalDutyPeriodBatchStatus.REQUESTED) }
    }
}
