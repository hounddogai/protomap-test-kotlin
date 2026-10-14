package com.acme.transport.network.servicearea.areaexception

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedServiceAreaMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedServiceAreaMapping("code-504", "label-356")
        val view = toCappedServiceAreaMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedServiceAreaMappingViews(mutableListOf<CappedServiceAreaMapping>(item, item)).size, "views")
    }
}
