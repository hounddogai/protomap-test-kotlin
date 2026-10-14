package com.acme.transport.network.cutoff.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class CutoffEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCutoffEntry("code-58", "name-519")
        val view = toCutoffEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCutoffEntryViews(mutableListOf<CutoffEntry>(item, item)).size, "views")
    }
}
