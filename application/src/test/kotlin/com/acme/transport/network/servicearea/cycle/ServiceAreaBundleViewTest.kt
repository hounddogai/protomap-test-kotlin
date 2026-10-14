package com.acme.transport.network.servicearea.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaBundle("code-757", "title-618")
        val view = toServiceAreaBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaBundleViews(mutableListOf<ServiceAreaBundle>(item, item)).size, "views")
    }
}
