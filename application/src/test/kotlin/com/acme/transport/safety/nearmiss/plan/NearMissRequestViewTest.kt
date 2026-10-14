package com.acme.transport.safety.nearmiss.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class NearMissRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNearMissRequest("code-373", "label-291")
        val view = toNearMissRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNearMissRequestViews(mutableListOf<NearMissRequest>(item, item)).size, "views")
    }
}
