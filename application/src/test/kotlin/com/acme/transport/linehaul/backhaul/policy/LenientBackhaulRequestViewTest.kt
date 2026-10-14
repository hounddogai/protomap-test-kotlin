package com.acme.transport.linehaul.backhaul.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientBackhaulRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientBackhaulRequest("code-844", "name-933")
        val view = toLenientBackhaulRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientBackhaulRequestViews(mutableListOf<LenientBackhaulRequest>(item, item)).size, "views")
    }
}
