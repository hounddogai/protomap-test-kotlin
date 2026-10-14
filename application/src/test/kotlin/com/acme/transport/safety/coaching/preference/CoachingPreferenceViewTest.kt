package com.acme.transport.safety.coaching.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class CoachingPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCoachingPreference("code-134", "name-701")
        val view = toCoachingPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCoachingPreferenceViews(mutableListOf<CoachingPreference>(item, item)).size, "views")
    }
}
