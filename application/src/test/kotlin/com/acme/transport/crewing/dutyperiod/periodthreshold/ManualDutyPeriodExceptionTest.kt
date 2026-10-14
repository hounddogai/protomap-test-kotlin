package com.acme.transport.crewing.dutyperiod.periodthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDutyPeriodExceptionTest {
    @Test
    fun `starts in requested`() {
        val item = newManualDutyPeriodException("code-710", "title-180")
        assertEquals(ManualDutyPeriodExceptionStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newManualDutyPeriodException("code-710", "title-180")
        try {
            item.moveTo(ManualDutyPeriodExceptionStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDutyPeriodExceptionStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newManualDutyPeriodException("code-710", "title-180")
        item.status = ManualDutyPeriodExceptionStatus.EXPIRED
        assertFails("move") { item.moveTo(ManualDutyPeriodExceptionStatus.REQUESTED) }
    }
}
