package com.acme.transport.crewing.dutyperiod.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodExceptionTest {
    @Test
    fun `starts in idle`() {
        val item = newDutyPeriodException("code-179", "label-516")
        assertEquals(DutyPeriodExceptionStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newDutyPeriodException("code-179", "label-516")
        try {
            item.moveTo(DutyPeriodExceptionStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodExceptionStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDutyPeriodException("code-179", "label-516")
        item.status = DutyPeriodExceptionStatus.STOPPED
        assertFails("move") { item.moveTo(DutyPeriodExceptionStatus.IDLE) }
    }
}
