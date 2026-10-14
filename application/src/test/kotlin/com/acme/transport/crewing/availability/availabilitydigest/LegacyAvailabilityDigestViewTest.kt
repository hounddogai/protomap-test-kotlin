package com.acme.transport.crewing.availability.availabilitydigest

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyAvailabilityDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyAvailabilityDigest("code-150", "name-223")
        val view = toLegacyAvailabilityDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyAvailabilityDigestViews(mutableListOf<LegacyAvailabilityDigest>(item, item)).size, "views")
    }
}
