package com.acme.transport.crewing.dutyperiod.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDutyPeriodSlotTest {
    @Test
    fun `starts in requested`() {
        val item = newManualDutyPeriodSlot("code-76", "label-538")
        assertEquals(ManualDutyPeriodSlotStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newManualDutyPeriodSlot("code-76", "label-538")
        try {
            item.moveTo(ManualDutyPeriodSlotStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDutyPeriodSlotStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newManualDutyPeriodSlot("code-76", "label-538")
        item.status = ManualDutyPeriodSlotStatus.EXPIRED
        assertFails("move") { item.moveTo(ManualDutyPeriodSlotStatus.REQUESTED) }
    }
}
