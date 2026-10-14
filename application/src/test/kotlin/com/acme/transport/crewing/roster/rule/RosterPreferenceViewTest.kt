package com.acme.transport.crewing.roster.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterPreference("code-689", "title-829")
        val view = toRosterPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterPreferenceViews(mutableListOf<RosterPreference>(item, item)).size, "views")
    }
}
