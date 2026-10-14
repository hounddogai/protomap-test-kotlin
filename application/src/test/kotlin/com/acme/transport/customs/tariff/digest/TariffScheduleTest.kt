package com.acme.transport.customs.tariff.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffScheduleTest {
    @Test
    fun `starts in draft`() {
        val item = newTariffSchedule("code-919", "title-155")
        assertEquals(TariffScheduleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newTariffSchedule("code-919", "title-155")
        try {
            item.moveTo(TariffScheduleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffScheduleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTariffSchedule("code-919", "title-155")
        item.status = TariffScheduleStatus.CLOSED
        assertFails("move") { item.moveTo(TariffScheduleStatus.DRAFT) }
    }
}
