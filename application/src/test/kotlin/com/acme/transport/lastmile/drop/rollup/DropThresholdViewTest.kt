package com.acme.transport.lastmile.drop.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class DropThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropThreshold("code-653", "title-233")
        val view = toDropThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropThresholdViews(mutableListOf<DropThreshold>(item, item)).size, "views")
    }
}
