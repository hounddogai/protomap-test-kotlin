package com.acme.transport.emissions.idling.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultIdlingSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultIdlingSegment("code-469", "title-458")
        val view = toDefaultIdlingSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultIdlingSegmentViews(mutableListOf<DefaultIdlingSegment>(item, item)).size, "views")
    }
}
