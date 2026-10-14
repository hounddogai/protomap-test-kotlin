package com.acme.transport.linehaul.backhaul.backhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictBackhaulVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictBackhaulVariance("code-19", "name-745")
        val view = toStrictBackhaulVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictBackhaulVarianceViews(mutableListOf<StrictBackhaulVariance>(item, item)).size, "views")
    }
}
