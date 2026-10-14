package com.acme.transport.crewing.availability.availabilitytier

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictAvailabilityNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictAvailabilityNotice("code-949", "title-536")
        val view = toStrictAvailabilityNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictAvailabilityNoticeViews(mutableListOf<StrictAvailabilityNotice>(item, item)).size, "views")
    }
}
