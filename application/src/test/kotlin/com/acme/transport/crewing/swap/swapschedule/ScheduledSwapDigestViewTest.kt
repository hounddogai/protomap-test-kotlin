package com.acme.transport.crewing.swap.swapschedule

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledSwapDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledSwapDigest("code-70", "title-683")
        val view = toScheduledSwapDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledSwapDigestViews(mutableListOf<ScheduledSwapDigest>(item, item)).size, "views")
    }
}
