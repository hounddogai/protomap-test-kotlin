package com.acme.transport.lastmile.parcel.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalParcelVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalParcelVariance("code-504", "name-863")
        val view = toRegionalParcelVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalParcelVarianceViews(mutableListOf<RegionalParcelVariance>(item, item)).size, "views")
    }
}
