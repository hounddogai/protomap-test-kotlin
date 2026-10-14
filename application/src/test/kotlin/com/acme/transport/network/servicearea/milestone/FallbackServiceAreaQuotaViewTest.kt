package com.acme.transport.network.servicearea.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackServiceAreaQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackServiceAreaQuota("code-575", "title-744")
        val view = toFallbackServiceAreaQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackServiceAreaQuotaViews(mutableListOf<FallbackServiceAreaQuota>(item, item)).size, "views")
    }
}
