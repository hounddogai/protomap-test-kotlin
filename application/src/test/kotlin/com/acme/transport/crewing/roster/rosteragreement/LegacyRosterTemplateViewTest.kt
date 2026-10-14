package com.acme.transport.crewing.roster.rosteragreement

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyRosterTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyRosterTemplate("code-709", "title-565")
        val view = toLegacyRosterTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyRosterTemplateViews(mutableListOf<LegacyRosterTemplate>(item, item)).size, "views")
    }
}
