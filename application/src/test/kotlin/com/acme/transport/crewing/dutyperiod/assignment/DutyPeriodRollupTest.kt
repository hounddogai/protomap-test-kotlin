package com.acme.transport.crewing.dutyperiod.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodRollupTest {
    @Test
    fun `starts in planned`() {
        val item = newDutyPeriodRollup("code-386", "label-953")
        assertEquals(DutyPeriodRollupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newDutyPeriodRollup("code-386", "label-953")
        try {
            item.moveTo(DutyPeriodRollupStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodRollupStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDutyPeriodRollup("code-386", "label-953")
        item.status = DutyPeriodRollupStatus.CANCELLED
        assertFails("move") { item.moveTo(DutyPeriodRollupStatus.PLANNED) }
    }
}
