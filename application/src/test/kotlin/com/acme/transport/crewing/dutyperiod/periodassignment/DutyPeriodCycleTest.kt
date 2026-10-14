package com.acme.transport.crewing.dutyperiod.periodassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodCycleTest {
    @Test
    fun `starts in idle`() {
        val item = newDutyPeriodCycle("code-956", "title-9")
        assertEquals(DutyPeriodCycleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newDutyPeriodCycle("code-956", "title-9")
        try {
            item.moveTo(DutyPeriodCycleStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodCycleStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDutyPeriodCycle("code-956", "title-9")
        item.status = DutyPeriodCycleStatus.STOPPED
        assertFails("move") { item.moveTo(DutyPeriodCycleStatus.IDLE) }
    }
}
