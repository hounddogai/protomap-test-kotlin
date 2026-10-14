package com.acme.transport.crewing.restbreak.breakrollup

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingRestBreakRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingRestBreakRollup("code-265", "title-817")
        val view = toRollingRestBreakRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingRestBreakRollupViews(mutableListOf<RollingRestBreakRollup>(item, item)).size, "views")
    }
}
