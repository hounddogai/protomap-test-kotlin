package com.acme.transport.fleetops.tire.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class TireExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireException("code-548", "label-668")
        val view = toTireExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireExceptionViews(mutableListOf<TireException>(item, item)).size, "views")
    }
}
