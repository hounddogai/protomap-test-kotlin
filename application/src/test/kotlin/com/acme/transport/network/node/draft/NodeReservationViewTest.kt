package com.acme.transport.network.node.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class NodeReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNodeReservation("code-741", "title-856")
        val view = toNodeReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNodeReservationViews(mutableListOf<NodeReservation>(item, item)).size, "views")
    }
}
