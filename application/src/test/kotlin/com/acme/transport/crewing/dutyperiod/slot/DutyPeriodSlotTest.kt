package com.acme.transport.crewing.dutyperiod.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodSlotTest {
    @Test
    fun `starts in planned`() {
        val item = newDutyPeriodSlot("code-352", "name-923")
        assertEquals(DutyPeriodSlotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newDutyPeriodSlot("code-352", "name-923")
        try {
            item.moveTo(DutyPeriodSlotStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodSlotStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDutyPeriodSlot("code-352", "name-923")
        item.status = DutyPeriodSlotStatus.CANCELLED
        assertFails("move") { item.moveTo(DutyPeriodSlotStatus.PLANNED) }
    }
}
