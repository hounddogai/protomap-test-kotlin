package com.acme.transport.linehaul.trailer.trailermilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingTrailerProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingTrailerProfile("code-216", "name-917")
        val view = toPendingTrailerProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingTrailerProfileViews(mutableListOf<PendingTrailerProfile>(item, item)).size, "views")
    }
}
