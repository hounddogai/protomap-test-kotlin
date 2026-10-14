package com.acme.transport.network.zone.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneDigest("code-597", "title-497")
        val view = toZoneDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneDigestViews(mutableListOf<ZoneDigest>(item, item)).size, "views")
    }
}
