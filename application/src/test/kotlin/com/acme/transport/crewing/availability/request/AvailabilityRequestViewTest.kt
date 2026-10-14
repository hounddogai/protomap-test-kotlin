package com.acme.transport.crewing.availability.request

import kotlin.test.Test
import kotlin.test.assertEquals

class AvailabilityRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAvailabilityRequest("code-524", "label-185")
        val view = toAvailabilityRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAvailabilityRequestViews(mutableListOf<AvailabilityRequest>(item, item)).size, "views")
    }
}
