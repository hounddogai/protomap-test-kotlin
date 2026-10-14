package com.acme.transport.linehaul.trailer.trailergroup

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledTrailerManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledTrailerManifest("code-1", "title-63")
        val view = toScheduledTrailerManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledTrailerManifestViews(mutableListOf<ScheduledTrailerManifest>(item, item)).size, "views")
    }
}
