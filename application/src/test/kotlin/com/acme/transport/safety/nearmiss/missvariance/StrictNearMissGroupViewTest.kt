package com.acme.transport.safety.nearmiss.missvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictNearMissGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictNearMissGroup("code-386", "name-417")
        val view = toStrictNearMissGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictNearMissGroupViews(mutableListOf<StrictNearMissGroup>(item, item)).size, "views")
    }
}
