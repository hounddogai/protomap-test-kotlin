package com.acme.transport.freightpay.accessorial.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedAccessorialVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedAccessorialVariance("code-774", "title-736")
        val view = toWeightedAccessorialVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedAccessorialVarianceViews(mutableListOf<WeightedAccessorialVariance>(item, item)).size, "views")
    }
}
