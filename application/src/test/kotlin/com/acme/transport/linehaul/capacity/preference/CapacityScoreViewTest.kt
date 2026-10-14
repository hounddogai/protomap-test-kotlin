package com.acme.transport.linehaul.capacity.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacityScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacityScore("code-779", "label-460")
        val view = toCapacityScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacityScoreViews(mutableListOf<CapacityScore>(item, item)).size, "views")
    }
}
