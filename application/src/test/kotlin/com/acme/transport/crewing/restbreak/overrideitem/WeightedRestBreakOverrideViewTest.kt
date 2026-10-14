package com.acme.transport.crewing.restbreak.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedRestBreakOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedRestBreakOverride("code-129", "label-87")
        val view = toWeightedRestBreakOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedRestBreakOverrideViews(mutableListOf<WeightedRestBreakOverride>(item, item)).size, "views")
    }
}
