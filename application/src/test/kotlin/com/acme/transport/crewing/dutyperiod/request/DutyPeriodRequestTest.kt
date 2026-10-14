package com.acme.transport.crewing.dutyperiod.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodRequestTest {
    @Test
    fun `starts in planned`() {
        val item = newDutyPeriodRequest("code-196", "name-158")
        assertEquals(DutyPeriodRequestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newDutyPeriodRequest("code-196", "name-158")
        try {
            item.moveTo(DutyPeriodRequestStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodRequestStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDutyPeriodRequest("code-196", "name-158")
        item.status = DutyPeriodRequestStatus.CANCELLED
        assertFails("move") { item.moveTo(DutyPeriodRequestStatus.PLANNED) }
    }
}
