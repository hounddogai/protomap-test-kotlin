package com.acme.transport.lastmile.redelivery.template

import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliveryPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRedeliveryPreference("code-490", "name-279")
        val view = toRedeliveryPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRedeliveryPreferenceViews(mutableListOf<RedeliveryPreference>(item, item)).size, "views")
    }
}
