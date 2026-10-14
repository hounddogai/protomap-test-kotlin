package com.acme.transport.customs.hold.holdslot

import kotlin.test.Test
import kotlin.test.assertEquals

class HoldAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHoldAudit("code-434", "name-276")
        val view = toHoldAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHoldAuditViews(mutableListOf<HoldAudit>(item, item)).size, "views")
    }
}
