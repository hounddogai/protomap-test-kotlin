package com.acme.transport.network.node.template

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalNodeTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalNodeTemplate("code-345", "label-881")
        val view = toSeasonalNodeTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalNodeTemplateViews(mutableListOf<SeasonalNodeTemplate>(item, item)).size, "views")
    }
}
