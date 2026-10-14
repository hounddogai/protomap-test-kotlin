package com.acme.transport.crewing.dutyperiod.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodAllocationTest {
    @Test
    fun `starts in draft`() {
        val item = newDutyPeriodAllocation("code-216", "name-711")
        assertEquals(DutyPeriodAllocationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newDutyPeriodAllocation("code-216", "name-711")
        try {
            item.moveTo(DutyPeriodAllocationStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodAllocationStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDutyPeriodAllocation("code-216", "name-711")
        item.status = DutyPeriodAllocationStatus.CLOSED
        assertFails("move") { item.moveTo(DutyPeriodAllocationStatus.DRAFT) }
    }
}
