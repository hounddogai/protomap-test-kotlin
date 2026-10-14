package com.acme.transport.freightpay.accessorial.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorialEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAccessorialEntry("code-243", "title-958")
        val view = toAccessorialEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAccessorialEntryViews(mutableListOf<AccessorialEntry>(item, item)).size, "views")
    }
}
