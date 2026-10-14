package com.acme.transport.crewing.availability.availabilityexception

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyAvailabilityExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyAvailabilityException("code-982", "name-315")
        val view = toLegacyAvailabilityExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyAvailabilityExceptionViews(mutableListOf<LegacyAvailabilityException>(item, item)).size, "views")
    }
}
