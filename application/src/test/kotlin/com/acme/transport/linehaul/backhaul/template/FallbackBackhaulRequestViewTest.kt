package com.acme.transport.linehaul.backhaul.template

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackBackhaulRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackBackhaulRequest("code-380", "title-495")
        val view = toFallbackBackhaulRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackBackhaulRequestViews(mutableListOf<FallbackBackhaulRequest>(item, item)).size, "views")
    }
}
