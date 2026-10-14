package com.acme.transport.lastmile.drop.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredDropThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredDropThreshold("code-213", "name-849")
        val view = toTieredDropThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredDropThresholdViews(mutableListOf<TieredDropThreshold>(item, item)).size, "views")
    }
}
