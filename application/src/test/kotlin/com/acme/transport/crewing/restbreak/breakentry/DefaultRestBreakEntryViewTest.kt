package com.acme.transport.crewing.restbreak.breakentry

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultRestBreakEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultRestBreakEntry("code-891", "name-666")
        val view = toDefaultRestBreakEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultRestBreakEntryViews(mutableListOf<DefaultRestBreakEntry>(item, item)).size, "views")
    }
}
