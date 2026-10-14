package com.acme.transport.crewing.availability.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class AvailabilityDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAvailabilityDigest("code-954", "label-379")
        val view = toAvailabilityDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAvailabilityDigestViews(mutableListOf<AvailabilityDigest>(item, item)).size, "views")
    }
}
