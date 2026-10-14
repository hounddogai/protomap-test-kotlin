package com.acme.transport.lastmile.parcel.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultParcelMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultParcelMilestone("code-276", "label-958")
        val view = toDefaultParcelMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultParcelMilestoneViews(mutableListOf<DefaultParcelMilestone>(item, item)).size, "views")
    }
}
