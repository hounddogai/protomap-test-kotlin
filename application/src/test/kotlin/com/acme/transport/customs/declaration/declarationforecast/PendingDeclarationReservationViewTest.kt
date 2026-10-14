package com.acme.transport.customs.declaration.declarationforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingDeclarationReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingDeclarationReservation("code-799", "label-205")
        val view = toPendingDeclarationReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingDeclarationReservationViews(mutableListOf<PendingDeclarationReservation>(item, item)).size, "views")
    }
}
