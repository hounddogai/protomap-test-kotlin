package com.acme.transport.lastmile.parcel.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelScheduleTest {
    @Test
    fun `starts in draft`() {
        val item = newParcelSchedule("code-390", "title-13")
        assertEquals(ParcelScheduleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newParcelSchedule("code-390", "title-13")
        try {
            item.moveTo(ParcelScheduleStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelScheduleStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newParcelSchedule("code-390", "title-13")
        item.status = ParcelScheduleStatus.CLOSED
        assertFails("move") { item.moveTo(ParcelScheduleStatus.DRAFT) }
    }
}
