package com.acme.transport.linehaul.capacity.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacityDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacityDraft("code-200", "title-753")
        val view = toCapacityDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacityDraftViews(mutableListOf<CapacityDraft>(item, item)).size, "views")
    }
}
