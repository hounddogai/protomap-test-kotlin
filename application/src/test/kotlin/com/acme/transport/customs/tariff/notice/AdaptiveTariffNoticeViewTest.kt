package com.acme.transport.customs.tariff.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveTariffNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveTariffNotice("code-519", "name-918")
        val view = toAdaptiveTariffNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveTariffNoticeViews(mutableListOf<AdaptiveTariffNotice>(item, item)).size, "views")
    }
}
