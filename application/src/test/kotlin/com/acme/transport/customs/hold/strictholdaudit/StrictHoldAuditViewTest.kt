package com.acme.transport.customs.hold.strictholdaudit

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictHoldAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictHoldAudit("code-249", "label-640")
        val view = toStrictHoldAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictHoldAuditViews(mutableListOf<StrictHoldAudit>(item, item)).size, "views")
    }
}
