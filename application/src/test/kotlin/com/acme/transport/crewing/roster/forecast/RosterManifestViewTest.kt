package com.acme.transport.crewing.roster.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterManifest("code-235", "title-754")
        val view = toRosterManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterManifestViews(mutableListOf<RosterManifest>(item, item)).size, "views")
    }
}
