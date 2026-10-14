package com.acme.transport.safety.violation.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class ViolationWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newViolationWindow("code-90", "name-792")
        val view = toViolationWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toViolationWindowViews(mutableListOf<ViolationWindow>(item, item)).size, "views")
    }
}
