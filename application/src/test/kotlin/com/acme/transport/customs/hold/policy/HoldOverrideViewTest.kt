package com.acme.transport.customs.hold.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class HoldOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHoldOverride("code-483", "title-997")
        val view = toHoldOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHoldOverrideViews(mutableListOf<HoldOverride>(item, item)).size, "views")
    }
}
