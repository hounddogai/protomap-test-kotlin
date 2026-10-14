package com.acme.transport.lastmile.parcel.template

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedParcelTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedParcelTarget("code-973", "name-740")
        val view = toWeightedParcelTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedParcelTargetViews(mutableListOf<WeightedParcelTarget>(item, item)).size, "views")
    }
}
