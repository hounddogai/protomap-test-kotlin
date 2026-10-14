package com.acme.transport.safety.violation.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class ViolationClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newViolationClaim("code-166", "title-500")
        val view = toViolationClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toViolationClaimViews(mutableListOf<ViolationClaim>(item, item)).size, "views")
    }
}
