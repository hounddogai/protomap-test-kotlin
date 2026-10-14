package com.acme.transport.lastmile.proofofdelivery.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientProofOfDeliveryReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientProofOfDeliveryReservation("code-936", "title-372")
        val view = toLenientProofOfDeliveryReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientProofOfDeliveryReservationViews(mutableListOf<LenientProofOfDeliveryReservation>(item, item)).size, "views")
    }
}
