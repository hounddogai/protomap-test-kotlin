package com.acme.transport.network.zone.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientZoneNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientZoneNotice("code-648", "name-563")
        val view = toLenientZoneNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientZoneNoticeViews(mutableListOf<LenientZoneNotice>(item, item)).size, "views")
    }
}
