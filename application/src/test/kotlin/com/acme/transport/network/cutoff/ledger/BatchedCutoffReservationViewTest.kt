package com.acme.transport.network.cutoff.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedCutoffReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedCutoffReservation("code-949", "name-694")
        val view = toBatchedCutoffReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedCutoffReservationViews(mutableListOf<BatchedCutoffReservation>(item, item)).size, "views")
    }
}
