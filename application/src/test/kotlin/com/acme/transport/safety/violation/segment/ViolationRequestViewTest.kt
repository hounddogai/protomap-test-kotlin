package com.acme.transport.safety.violation.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class ViolationRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newViolationRequest("code-509", "name-302")
        val view = toViolationRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toViolationRequestViews(mutableListOf<ViolationRequest>(item, item)).size, "views")
    }
}
