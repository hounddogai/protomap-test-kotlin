package com.acme.transport.network.servicearea.primaryareatarget

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingServiceAreaTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingServiceAreaTarget("code-948", "name-871")
        val view = toRollingServiceAreaTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingServiceAreaTargetViews(mutableListOf<RollingServiceAreaTarget>(item, item)).size, "views")
    }
}
