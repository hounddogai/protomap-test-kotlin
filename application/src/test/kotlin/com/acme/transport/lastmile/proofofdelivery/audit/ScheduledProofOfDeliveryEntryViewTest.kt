package com.acme.transport.lastmile.proofofdelivery.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledProofOfDeliveryEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledProofOfDeliveryEntry("code-705", "name-481")
        val view = toScheduledProofOfDeliveryEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledProofOfDeliveryEntryViews(mutableListOf<ScheduledProofOfDeliveryEntry>(item, item)).size, "views")
    }
}
