package com.acme.transport.crewing.swap.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class SwapQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSwapQuota("code-601", "title-780")
        val view = toSwapQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSwapQuotaViews(mutableListOf<SwapQuota>(item, item)).size, "views")
    }
}
