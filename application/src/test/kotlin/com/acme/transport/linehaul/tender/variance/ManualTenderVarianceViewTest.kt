package com.acme.transport.linehaul.tender.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualTenderVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualTenderVariance("code-930", "label-833")
        val view = toManualTenderVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualTenderVarianceViews(mutableListOf<ManualTenderVariance>(item, item)).size, "views")
    }
}
