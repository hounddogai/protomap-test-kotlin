package com.acme.transport.network.servicearea.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaTemplate("code-153", "title-659")
        val view = toServiceAreaTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaTemplateViews(mutableListOf<ServiceAreaTemplate>(item, item)).size, "views")
    }
}
