package com.acme.transport.lastmile.redelivery.redeliveryentry

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalRedeliveryEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalRedeliveryEntry("code-172", "label-76")
        val view = toRegionalRedeliveryEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalRedeliveryEntryViews(mutableListOf<RegionalRedeliveryEntry>(item, item)).size, "views")
    }
}
