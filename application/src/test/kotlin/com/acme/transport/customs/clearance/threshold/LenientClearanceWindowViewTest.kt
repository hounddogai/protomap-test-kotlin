package com.acme.transport.customs.clearance.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientClearanceWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientClearanceWindow("code-930", "label-152")
        val view = toLenientClearanceWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientClearanceWindowViews(mutableListOf<LenientClearanceWindow>(item, item)).size, "views")
    }
}
