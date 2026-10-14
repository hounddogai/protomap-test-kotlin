package com.acme.transport.emissions.idling.idlingvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultIdlingVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultIdlingVariance("code-717", "name-511")
        val view = toDefaultIdlingVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultIdlingVarianceViews(mutableListOf<DefaultIdlingVariance>(item, item)).size, "views")
    }
}
