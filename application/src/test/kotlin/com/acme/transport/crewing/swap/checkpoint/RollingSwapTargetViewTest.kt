package com.acme.transport.crewing.swap.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingSwapTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingSwapTarget("code-460", "name-659")
        val view = toRollingSwapTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingSwapTargetViews(mutableListOf<RollingSwapTarget>(item, item)).size, "views")
    }
}
