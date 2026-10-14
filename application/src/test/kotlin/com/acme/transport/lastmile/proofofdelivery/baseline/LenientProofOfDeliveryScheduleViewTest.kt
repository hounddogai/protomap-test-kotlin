package com.acme.transport.lastmile.proofofdelivery.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientProofOfDeliveryScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientProofOfDeliverySchedule("code-757", "label-389")
        val view = toLenientProofOfDeliveryScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientProofOfDeliveryScheduleViews(mutableListOf<LenientProofOfDeliverySchedule>(item, item)).size, "views")
    }
}
