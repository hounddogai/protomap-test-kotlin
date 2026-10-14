package com.acme.transport.linehaul.laneplan.item

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredLanePlanOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredLanePlanOverride("code-975", "title-565")
        val view = toTieredLanePlanOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredLanePlanOverrideViews(mutableListOf<TieredLanePlanOverride>(item, item)).size, "views")
    }
}
