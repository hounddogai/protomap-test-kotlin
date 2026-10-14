package com.acme.transport.crewing.dutyperiod.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodSummaryTest {
    @Test
    fun `starts in idle`() {
        val item = newDutyPeriodSummary("code-191", "title-647")
        assertEquals(DutyPeriodSummaryStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newDutyPeriodSummary("code-191", "title-647")
        try {
            item.moveTo(DutyPeriodSummaryStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodSummaryStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDutyPeriodSummary("code-191", "title-647")
        item.status = DutyPeriodSummaryStatus.STOPPED
        assertFails("move") { item.moveTo(DutyPeriodSummaryStatus.IDLE) }
    }
}
