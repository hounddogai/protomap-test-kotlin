package com.acme.transport.emissions.evcharge.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class EvChargeExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEvChargeException("code-434", "label-688")
        val view = toEvChargeExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEvChargeExceptionViews(mutableListOf<EvChargeException>(item, item)).size, "views")
    }
}
