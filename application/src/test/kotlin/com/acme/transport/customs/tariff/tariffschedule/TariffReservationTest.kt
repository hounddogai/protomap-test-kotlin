package com.acme.transport.customs.tariff.tariffschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffReservationTest {
    @Test
    fun `starts in created`() {
        val item = newTariffReservation("code-438", "name-841")
        assertEquals(TariffReservationStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newTariffReservation("code-438", "name-841")
        try {
            item.moveTo(TariffReservationStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffReservationStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTariffReservation("code-438", "name-841")
        item.status = TariffReservationStatus.RETIRED
        assertFails("move") { item.moveTo(TariffReservationStatus.CREATED) }
    }
}
