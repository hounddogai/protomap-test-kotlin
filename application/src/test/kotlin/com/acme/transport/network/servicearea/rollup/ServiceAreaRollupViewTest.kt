package com.acme.transport.network.servicearea.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaRollup("code-808", "label-37")
        val view = toServiceAreaRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaRollupViews(mutableListOf<ServiceAreaRollup>(item, item)).size, "views")
    }
}
