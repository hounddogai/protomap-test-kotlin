package com.acme.transport.safety.nearmiss.missadjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledNearMissQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledNearMissQuota("code-515", "name-201")
        val view = toScheduledNearMissQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledNearMissQuotaViews(mutableListOf<ScheduledNearMissQuota>(item, item)).size, "views")
    }
}
