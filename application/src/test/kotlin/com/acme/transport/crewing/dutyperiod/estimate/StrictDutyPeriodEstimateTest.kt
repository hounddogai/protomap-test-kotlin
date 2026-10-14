package com.acme.transport.crewing.dutyperiod.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDutyPeriodEstimateTest {
    @Test
    fun `starts in open`() {
        val item = newStrictDutyPeriodEstimate("code-104", "name-926")
        assertEquals(StrictDutyPeriodEstimateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newStrictDutyPeriodEstimate("code-104", "name-926")
        try {
            item.moveTo(StrictDutyPeriodEstimateStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDutyPeriodEstimateStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictDutyPeriodEstimate("code-104", "name-926")
        item.status = StrictDutyPeriodEstimateStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictDutyPeriodEstimateStatus.OPEN) }
    }
}
