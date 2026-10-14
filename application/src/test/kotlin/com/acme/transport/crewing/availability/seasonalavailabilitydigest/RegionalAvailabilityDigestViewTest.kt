package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalAvailabilityDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalAvailabilityDigest("code-666", "label-638")
        val view = toRegionalAvailabilityDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalAvailabilityDigestViews(mutableListOf<RegionalAvailabilityDigest>(item, item)).size, "views")
    }
}
