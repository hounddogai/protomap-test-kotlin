package com.acme.transport.linehaul.capacity.capacityassignment

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacityDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacityDigest("code-431", "title-776")
        val view = toCapacityDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacityDigestViews(mutableListOf<CapacityDigest>(item, item)).size, "views")
    }
}
