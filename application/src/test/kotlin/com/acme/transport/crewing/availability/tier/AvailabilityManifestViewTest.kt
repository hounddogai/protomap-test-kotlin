package com.acme.transport.crewing.availability.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class AvailabilityManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAvailabilityManifest("code-904", "name-90")
        val view = toAvailabilityManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAvailabilityManifestViews(mutableListOf<AvailabilityManifest>(item, item)).size, "views")
    }
}
