package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultAccessorialCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultAccessorialCycle("code-693", "name-541")
        val view = toDefaultAccessorialCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultAccessorialCycleViews(mutableListOf<DefaultAccessorialCycle>(item, item)).size, "views")
    }
}
