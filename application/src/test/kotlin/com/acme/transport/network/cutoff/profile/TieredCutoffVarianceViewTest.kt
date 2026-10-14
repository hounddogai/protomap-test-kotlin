package com.acme.transport.network.cutoff.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredCutoffVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredCutoffVariance("code-889", "title-777")
        val view = toTieredCutoffVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredCutoffVarianceViews(mutableListOf<TieredCutoffVariance>(item, item)).size, "views")
    }
}
