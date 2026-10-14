package com.acme.transport.lastmile.redelivery.redeliveryquota

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalRedeliveryGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalRedeliveryGroup("code-451", "label-605")
        val view = toSeasonalRedeliveryGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalRedeliveryGroupViews(mutableListOf<SeasonalRedeliveryGroup>(item, item)).size, "views")
    }
}
