package com.acme.transport.network.servicearea.primaryareatarget

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictServiceAreaQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictServiceAreaQuota("code-111", "name-53")
        val view = toStrictServiceAreaQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictServiceAreaQuotaViews(mutableListOf<StrictServiceAreaQuota>(item, item)).size, "views")
    }
}
