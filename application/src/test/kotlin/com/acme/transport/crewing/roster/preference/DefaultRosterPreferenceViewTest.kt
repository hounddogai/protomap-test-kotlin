package com.acme.transport.crewing.roster.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultRosterPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultRosterPreference("code-204", "title-233")
        val view = toDefaultRosterPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultRosterPreferenceViews(mutableListOf<DefaultRosterPreference>(item, item)).size, "views")
    }
}
