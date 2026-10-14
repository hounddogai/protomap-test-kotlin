package com.acme.transport.safety.violation.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class ViolationBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newViolationBundle("code-604", "name-521")
        val view = toViolationBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toViolationBundleViews(mutableListOf<ViolationBundle>(item, item)).size, "views")
    }
}
