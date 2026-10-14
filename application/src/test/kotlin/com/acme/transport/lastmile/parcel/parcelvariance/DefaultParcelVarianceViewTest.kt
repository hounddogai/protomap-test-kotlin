package com.acme.transport.lastmile.parcel.parcelvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultParcelVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultParcelVariance("code-22", "name-534")
        val view = toDefaultParcelVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultParcelVarianceViews(mutableListOf<DefaultParcelVariance>(item, item)).size, "views")
    }
}
