package com.acme.transport.linehaul.trailer.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledTrailerOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledTrailerOverride("code-329", "name-57")
        val view = toScheduledTrailerOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledTrailerOverrideViews(mutableListOf<ScheduledTrailerOverride>(item, item)).size, "views")
    }
}
