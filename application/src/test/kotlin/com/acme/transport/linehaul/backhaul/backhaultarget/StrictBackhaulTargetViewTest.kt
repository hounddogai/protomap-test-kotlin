package com.acme.transport.linehaul.backhaul.backhaultarget

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictBackhaulTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictBackhaulTarget("code-462", "title-647")
        val view = toStrictBackhaulTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictBackhaulTargetViews(mutableListOf<StrictBackhaulTarget>(item, item)).size, "views")
    }
}
