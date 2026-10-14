package com.acme.transport.linehaul.capacity.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedCapacityReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedCapacityReservation("code-229", "title-807")
        val view = toBatchedCapacityReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedCapacityReservationViews(mutableListOf<BatchedCapacityReservation>(item, item)).size, "views")
    }
}
