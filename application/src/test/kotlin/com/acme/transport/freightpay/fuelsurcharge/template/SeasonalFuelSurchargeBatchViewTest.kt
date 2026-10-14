package com.acme.transport.freightpay.fuelsurcharge.template

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalFuelSurchargeBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalFuelSurchargeBatch("code-933", "name-84")
        val view = toSeasonalFuelSurchargeBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalFuelSurchargeBatchViews(mutableListOf<SeasonalFuelSurchargeBatch>(item, item)).size, "views")
    }
}
