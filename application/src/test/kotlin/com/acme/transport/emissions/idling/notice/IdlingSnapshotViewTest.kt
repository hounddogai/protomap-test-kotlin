package com.acme.transport.emissions.idling.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingSnapshot("code-222", "title-603")
        val view = toIdlingSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingSnapshotViews(mutableListOf<IdlingSnapshot>(item, item)).size, "views")
    }
}
