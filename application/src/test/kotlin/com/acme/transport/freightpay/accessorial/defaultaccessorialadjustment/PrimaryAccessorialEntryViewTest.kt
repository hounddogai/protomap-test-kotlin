package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryAccessorialEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryAccessorialEntry("code-540", "name-532")
        val view = toPrimaryAccessorialEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryAccessorialEntryViews(mutableListOf<PrimaryAccessorialEntry>(item, item)).size, "views")
    }
}
