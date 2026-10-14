package com.acme.transport.emissions.idling.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingEntry("code-192", "name-177")
        val view = toIdlingEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingEntryViews(mutableListOf<IdlingEntry>(item, item)).size, "views")
    }
}
