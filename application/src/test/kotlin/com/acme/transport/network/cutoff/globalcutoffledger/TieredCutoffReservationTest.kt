package com.acme.transport.network.cutoff.globalcutoffledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredCutoffReservationTest {
    @Test
    fun `starts in open`() {
        val item = newTieredCutoffReservation("code-969", "name-126")
        assertEquals(TieredCutoffReservationStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newTieredCutoffReservation("code-969", "name-126")
        try {
            item.moveTo(TieredCutoffReservationStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredCutoffReservationStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTieredCutoffReservation("code-969", "name-126")
        item.status = TieredCutoffReservationStatus.ARCHIVED
        assertFails("move") { item.moveTo(TieredCutoffReservationStatus.OPEN) }
    }
}
