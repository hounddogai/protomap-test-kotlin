package com.acme.transport.linehaul.relay.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalRelayNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalRelayNotice("code-364", "name-937")
        val view = toRegionalRelayNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalRelayNoticeViews(mutableListOf<RegionalRelayNotice>(item, item)).size, "views")
    }
}
