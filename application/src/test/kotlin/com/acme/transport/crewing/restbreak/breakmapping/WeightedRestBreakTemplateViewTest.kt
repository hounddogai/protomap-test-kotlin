package com.acme.transport.crewing.restbreak.breakmapping

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedRestBreakTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedRestBreakTemplate("code-636", "label-705")
        val view = toWeightedRestBreakTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedRestBreakTemplateViews(mutableListOf<WeightedRestBreakTemplate>(item, item)).size, "views")
    }
}
