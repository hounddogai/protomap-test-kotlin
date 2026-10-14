package com.acme.transport.lastmile.locker.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class LockerAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLockerAudit("code-663", "name-115")
        val view = toLockerAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLockerAuditViews(mutableListOf<LockerAudit>(item, item)).size, "views")
    }
}
