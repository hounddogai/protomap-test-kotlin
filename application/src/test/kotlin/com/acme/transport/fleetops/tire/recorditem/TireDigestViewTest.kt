package com.acme.transport.fleetops.tire.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class TireDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireDigest("code-386", "name-625")
        val view = toTireDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireDigestViews(mutableListOf<TireDigest>(item, item)).size, "views")
    }
}
