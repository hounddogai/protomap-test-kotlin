package com.acme.transport.safety.violation.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class ViolationEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newViolationEstimate("code-213", "name-879")
        val view = toViolationEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toViolationEstimateViews(mutableListOf<ViolationEstimate>(item, item)).size, "views")
    }
}
