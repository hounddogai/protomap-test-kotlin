package com.acme.transport.crewing.swap.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingSwapSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingSwapSegment("code-534", "name-829")
        val view = toRollingSwapSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingSwapSegmentViews(mutableListOf<RollingSwapSegment>(item, item)).size, "views")
    }
}
