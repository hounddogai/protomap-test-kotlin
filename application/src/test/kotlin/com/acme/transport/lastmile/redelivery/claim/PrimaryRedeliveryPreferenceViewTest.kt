package com.acme.transport.lastmile.redelivery.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryRedeliveryPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryRedeliveryPreference("code-846", "name-810")
        val view = toPrimaryRedeliveryPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryRedeliveryPreferenceViews(mutableListOf<PrimaryRedeliveryPreference>(item, item)).size, "views")
    }
}
