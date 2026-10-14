package com.acme.transport.crewing.dutyperiod.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodMetricTest {
    @Test
    fun `starts in requested`() {
        val item = newDutyPeriodMetric("code-393", "title-185")
        assertEquals(DutyPeriodMetricStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDutyPeriodMetric("code-393", "title-185")
        try {
            item.moveTo(DutyPeriodMetricStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodMetricStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDutyPeriodMetric("code-393", "title-185")
        item.status = DutyPeriodMetricStatus.EXPIRED
        assertFails("move") { item.moveTo(DutyPeriodMetricStatus.REQUESTED) }
    }
}
