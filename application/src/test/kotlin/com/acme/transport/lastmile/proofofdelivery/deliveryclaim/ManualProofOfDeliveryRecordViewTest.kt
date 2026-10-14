package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualProofOfDeliveryRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualProofOfDeliveryRecord("code-429", "title-870")
        val view = toManualProofOfDeliveryRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualProofOfDeliveryRecordViews(mutableListOf<ManualProofOfDeliveryRecord>(item, item)).size, "views")
    }
}
