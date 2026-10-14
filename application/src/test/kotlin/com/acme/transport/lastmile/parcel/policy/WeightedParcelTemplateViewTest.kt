package com.acme.transport.lastmile.parcel.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedParcelTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedParcelTemplate("code-811", "label-394")
        val view = toWeightedParcelTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedParcelTemplateViews(mutableListOf<WeightedParcelTemplate>(item, item)).size, "views")
    }
}
