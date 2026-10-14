package com.acme.transport.lastmile.proofofdelivery.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryProofOfDeliveryNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryProofOfDeliveryNotice("code-54", "name-773")
        val view = toPrimaryProofOfDeliveryNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryProofOfDeliveryNoticeViews(mutableListOf<PrimaryProofOfDeliveryNotice>(item, item)).size, "views")
    }
}
