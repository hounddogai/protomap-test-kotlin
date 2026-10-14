package com.acme.transport.crewing.roster.fallbackrosterforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictRosterManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictRosterManifest("code-62", "label-367")
        val view = toStrictRosterManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictRosterManifestViews(mutableListOf<StrictRosterManifest>(item, item)).size, "views")
    }
}
