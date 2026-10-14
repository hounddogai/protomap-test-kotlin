package com.acme.transport.linehaul.tender.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedTenderTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedTenderTemplate("code-607", "name-781")
        val view = toWeightedTenderTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedTenderTemplateViews(mutableListOf<WeightedTenderTemplate>(item, item)).size, "views")
    }
}
