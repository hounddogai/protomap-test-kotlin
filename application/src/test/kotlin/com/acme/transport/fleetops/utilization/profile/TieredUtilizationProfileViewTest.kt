package com.acme.transport.fleetops.utilization.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredUtilizationProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredUtilizationProfile("code-637", "name-317")
        val view = toTieredUtilizationProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredUtilizationProfileViews(mutableListOf<TieredUtilizationProfile>(item, item)).size, "views")
    }
}
