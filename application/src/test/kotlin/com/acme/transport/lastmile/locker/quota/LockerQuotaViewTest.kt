package com.acme.transport.lastmile.locker.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class LockerQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLockerQuota("code-975", "title-444")
        val view = toLockerQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLockerQuotaViews(mutableListOf<LockerQuota>(item, item)).size, "views")
    }
}
