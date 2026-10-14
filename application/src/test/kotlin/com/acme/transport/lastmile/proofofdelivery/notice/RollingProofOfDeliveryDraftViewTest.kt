package com.acme.transport.lastmile.proofofdelivery.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingProofOfDeliveryDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingProofOfDeliveryDraft("code-831", "name-503")
        val view = toRollingProofOfDeliveryDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingProofOfDeliveryDraftViews(mutableListOf<RollingProofOfDeliveryDraft>(item, item)).size, "views")
    }
}
