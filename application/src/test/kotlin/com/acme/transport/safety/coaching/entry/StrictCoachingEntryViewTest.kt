package com.acme.transport.safety.coaching.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictCoachingEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictCoachingEntry("code-67", "name-480")
        val view = toStrictCoachingEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictCoachingEntryViews(mutableListOf<StrictCoachingEntry>(item, item)).size, "views")
    }
}
