package com.acme.transport.customs.hold.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class HoldTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHoldTarget("code-530", "name-566")
        val view = toHoldTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHoldTargetViews(mutableListOf<HoldTarget>(item, item)).size, "views")
    }
}
