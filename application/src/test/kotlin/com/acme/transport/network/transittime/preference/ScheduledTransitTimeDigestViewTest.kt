package com.acme.transport.network.transittime.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledTransitTimeDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledTransitTimeDigest("code-913", "title-73")
        val view = toScheduledTransitTimeDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledTransitTimeDigestViews(mutableListOf<ScheduledTransitTimeDigest>(item, item)).size, "views")
    }
}
