package com.acme.transport.lastmile.drop.dropforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryDropThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryDropThreshold("code-750", "name-469")
        val view = toPrimaryDropThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryDropThresholdViews(mutableListOf<PrimaryDropThreshold>(item, item)).size, "views")
    }
}
