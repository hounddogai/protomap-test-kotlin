package com.acme.transport.lastmile.redelivery.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalRedeliveryPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalRedeliveryPreference("code-28", "name-594")
        val view = toRegionalRedeliveryPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalRedeliveryPreferenceViews(mutableListOf<RegionalRedeliveryPreference>(item, item)).size, "views")
    }
}
