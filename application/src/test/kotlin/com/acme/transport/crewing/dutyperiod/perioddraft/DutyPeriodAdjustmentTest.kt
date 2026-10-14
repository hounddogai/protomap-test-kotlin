package com.acme.transport.crewing.dutyperiod.perioddraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodAdjustmentTest {
    @Test
    fun `starts in draft`() {
        val item = newDutyPeriodAdjustment("code-483", "label-184")
        assertEquals(DutyPeriodAdjustmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newDutyPeriodAdjustment("code-483", "label-184")
        try {
            item.moveTo(DutyPeriodAdjustmentStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodAdjustmentStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDutyPeriodAdjustment("code-483", "label-184")
        item.status = DutyPeriodAdjustmentStatus.CLOSED
        assertFails("move") { item.moveTo(DutyPeriodAdjustmentStatus.DRAFT) }
    }
}
