package com.acme.transport.crewing.availability.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class AvailabilityProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAvailabilityProfile("code-498", "title-822")
        val view = toAvailabilityProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAvailabilityProfileViews(mutableListOf<AvailabilityProfile>(item, item)).size, "views")
    }
}
