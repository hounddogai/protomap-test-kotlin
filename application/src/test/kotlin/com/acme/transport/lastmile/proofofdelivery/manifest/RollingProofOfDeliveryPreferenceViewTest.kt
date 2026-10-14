package com.acme.transport.lastmile.proofofdelivery.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingProofOfDeliveryPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingProofOfDeliveryPreference("code-816", "name-369")
        val view = toRollingProofOfDeliveryPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingProofOfDeliveryPreferenceViews(mutableListOf<RollingProofOfDeliveryPreference>(item, item)).size, "views")
    }
}
