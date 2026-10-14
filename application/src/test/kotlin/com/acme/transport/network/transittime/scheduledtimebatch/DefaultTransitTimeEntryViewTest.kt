package com.acme.transport.network.transittime.scheduledtimebatch

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultTransitTimeEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultTransitTimeEntry("code-91", "title-764")
        val view = toDefaultTransitTimeEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultTransitTimeEntryViews(mutableListOf<DefaultTransitTimeEntry>(item, item)).size, "views")
    }
}
