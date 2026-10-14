package com.acme.transport.emissions.idling.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultIdlingEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultIdlingEntry("code-825", "name-700")
        val view = toDefaultIdlingEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultIdlingEntryViews(mutableListOf<DefaultIdlingEntry>(item, item)).size, "views")
    }
}
