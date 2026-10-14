package com.acme.transport.emissions.idling.idlingmanifest

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledIdlingManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledIdlingManifest("code-122", "title-616")
        val view = toScheduledIdlingManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledIdlingManifestViews(mutableListOf<ScheduledIdlingManifest>(item, item)).size, "views")
    }
}
