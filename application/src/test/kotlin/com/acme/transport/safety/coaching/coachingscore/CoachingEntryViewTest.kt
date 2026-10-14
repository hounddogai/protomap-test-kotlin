package com.acme.transport.safety.coaching.coachingscore

import kotlin.test.Test
import kotlin.test.assertEquals

class CoachingEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCoachingEntry("code-687", "title-363")
        val view = toCoachingEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCoachingEntryViews(mutableListOf<CoachingEntry>(item, item)).size, "views")
    }
}
