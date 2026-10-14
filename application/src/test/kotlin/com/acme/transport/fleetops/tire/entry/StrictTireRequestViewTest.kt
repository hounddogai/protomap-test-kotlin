package com.acme.transport.fleetops.tire.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictTireRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictTireRequest("code-710", "title-597")
        val view = toStrictTireRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictTireRequestViews(mutableListOf<StrictTireRequest>(item, item)).size, "views")
    }
}
