package com.acme.transport.emissions.fuelburn.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnScheduleTest {
    @Test
    fun `starts in requested`() {
        val item = newFuelBurnSchedule("code-254", "label-294")
        assertEquals(FuelBurnScheduleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newFuelBurnSchedule("code-254", "label-294")
        try {
            item.moveTo(FuelBurnScheduleStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnScheduleStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFuelBurnSchedule("code-254", "label-294")
        item.status = FuelBurnScheduleStatus.EXPIRED
        assertFails("move") { item.moveTo(FuelBurnScheduleStatus.REQUESTED) }
    }
}
