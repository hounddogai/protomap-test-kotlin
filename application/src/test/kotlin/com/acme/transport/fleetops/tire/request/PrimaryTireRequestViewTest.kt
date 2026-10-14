package com.acme.transport.fleetops.tire.request

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryTireRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryTireRequest("code-657", "title-171")
        val view = toPrimaryTireRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryTireRequestViews(mutableListOf<PrimaryTireRequest>(item, item)).size, "views")
    }
}
