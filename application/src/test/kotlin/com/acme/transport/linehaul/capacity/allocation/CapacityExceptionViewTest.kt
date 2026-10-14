package com.acme.transport.linehaul.capacity.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacityExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacityException("code-840", "name-898")
        val view = toCapacityExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacityExceptionViews(mutableListOf<CapacityException>(item, item)).size, "views")
    }
}
