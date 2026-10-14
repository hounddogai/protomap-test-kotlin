package com.acme.transport.emissions.idling.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingQuota("code-165", "label-110")
        val view = toIdlingQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingQuotaViews(mutableListOf<IdlingQuota>(item, item)).size, "views")
    }
}
