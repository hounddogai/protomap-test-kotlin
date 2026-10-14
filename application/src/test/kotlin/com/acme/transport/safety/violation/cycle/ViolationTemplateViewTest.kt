package com.acme.transport.safety.violation.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class ViolationTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newViolationTemplate("code-896", "label-353")
        val view = toViolationTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toViolationTemplateViews(mutableListOf<ViolationTemplate>(item, item)).size, "views")
    }
}
