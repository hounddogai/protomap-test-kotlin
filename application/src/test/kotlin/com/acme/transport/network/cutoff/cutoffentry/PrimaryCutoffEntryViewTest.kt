package com.acme.transport.network.cutoff.cutoffentry

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryCutoffEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryCutoffEntry("code-367", "title-623")
        val view = toPrimaryCutoffEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryCutoffEntryViews(mutableListOf<PrimaryCutoffEntry>(item, item)).size, "views")
    }
}
