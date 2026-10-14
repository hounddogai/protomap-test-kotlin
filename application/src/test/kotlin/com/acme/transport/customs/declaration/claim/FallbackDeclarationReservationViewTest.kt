package com.acme.transport.customs.declaration.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackDeclarationReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackDeclarationReservation("code-807", "name-202")
        val view = toFallbackDeclarationReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackDeclarationReservationViews(mutableListOf<FallbackDeclarationReservation>(item, item)).size, "views")
    }
}
