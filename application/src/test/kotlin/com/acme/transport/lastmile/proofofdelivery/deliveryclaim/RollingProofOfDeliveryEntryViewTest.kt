package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingProofOfDeliveryEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingProofOfDeliveryEntry("code-578", "title-896")
        val view = toRollingProofOfDeliveryEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingProofOfDeliveryEntryViews(mutableListOf<RollingProofOfDeliveryEntry>(item, item)).size, "views")
    }
}
