package com.acme.transport.linehaul.backhaul.backhauldigest

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveBackhaulQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveBackhaulQuota("code-206", "name-636")
        val view = toAdaptiveBackhaulQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveBackhaulQuotaViews(mutableListOf<AdaptiveBackhaulQuota>(item, item)).size, "views")
    }
}
