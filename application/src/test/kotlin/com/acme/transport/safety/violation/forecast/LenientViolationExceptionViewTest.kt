package com.acme.transport.safety.violation.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientViolationExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientViolationException("code-613", "title-626")
        val view = toLenientViolationExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientViolationExceptionViews(mutableListOf<LenientViolationException>(item, item)).size, "views")
    }
}
