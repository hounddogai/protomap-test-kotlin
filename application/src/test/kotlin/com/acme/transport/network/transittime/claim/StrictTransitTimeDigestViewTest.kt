package com.acme.transport.network.transittime.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictTransitTimeDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictTransitTimeDigest("code-99", "title-838")
        val view = toStrictTransitTimeDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictTransitTimeDigestViews(mutableListOf<StrictTransitTimeDigest>(item, item)).size, "views")
    }
}
