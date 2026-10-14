package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedBackhaulVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedBackhaulVariance("code-168", "title-53")
        val view = toCappedBackhaulVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedBackhaulVarianceViews(mutableListOf<CappedBackhaulVariance>(item, item)).size, "views")
    }
}
