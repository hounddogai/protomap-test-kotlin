package com.acme.transport.customs.tariff.tariffschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackTariffScheduleTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackTariffSchedule("code-840", "name-157")
        assertEquals(FallbackTariffScheduleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newFallbackTariffSchedule("code-840", "name-157")
        try {
            item.moveTo(FallbackTariffScheduleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackTariffScheduleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackTariffSchedule("code-840", "name-157")
        item.status = FallbackTariffScheduleStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackTariffScheduleStatus.DRAFT) }
    }
}
