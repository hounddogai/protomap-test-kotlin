package com.acme.transport.fleetops.tire.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class TireScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireScore("code-380", "title-717")
        val view = toTireScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireScoreViews(mutableListOf<TireScore>(item, item)).size, "views")
    }
}
