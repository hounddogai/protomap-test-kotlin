package com.acme.transport.linehaul.backhaul.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class BackhaulClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBackhaulClaim("code-751", "name-191")
        val view = toBackhaulClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBackhaulClaimViews(mutableListOf<BackhaulClaim>(item, item)).size, "views")
    }
}
