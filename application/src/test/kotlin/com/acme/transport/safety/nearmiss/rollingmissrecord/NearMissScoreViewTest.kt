package com.acme.transport.safety.nearmiss.rollingmissrecord

import kotlin.test.Test
import kotlin.test.assertEquals

class NearMissScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNearMissScore("code-506", "name-927")
        val view = toNearMissScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNearMissScoreViews(mutableListOf<NearMissScore>(item, item)).size, "views")
    }
}
