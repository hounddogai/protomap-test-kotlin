package com.acme.transport.fleetops.tire.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class TireNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireNotice("code-304", "name-748")
        val view = toTireNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireNoticeViews(mutableListOf<TireNotice>(item, item)).size, "views")
    }
}
