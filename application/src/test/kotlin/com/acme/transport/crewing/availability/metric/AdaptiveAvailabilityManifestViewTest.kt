package com.acme.transport.crewing.availability.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveAvailabilityManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveAvailabilityManifest("code-819", "label-909")
        val view = toAdaptiveAvailabilityManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveAvailabilityManifestViews(mutableListOf<AdaptiveAvailabilityManifest>(item, item)).size, "views")
    }
}
