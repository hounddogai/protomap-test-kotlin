package com.acme.transport.safety.nearmiss.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class NearMissGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNearMissGroup("code-348", "title-658")
        val view = toNearMissGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNearMissGroupViews(mutableListOf<NearMissGroup>(item, item)).size, "views")
    }
}
