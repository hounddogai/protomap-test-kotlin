package com.acme.transport.safety.violation.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientViolationCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientViolationCycle("code-321", "label-977")
        val view = toLenientViolationCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientViolationCycleViews(mutableListOf<LenientViolationCycle>(item, item)).size, "views")
    }
}
