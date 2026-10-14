package com.acme.transport.terminal.dwell.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingDwellMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingDwellMapping("code-578", "label-151")
        val view = toRollingDwellMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingDwellMappingViews(mutableListOf<RollingDwellMapping>(item, item)).size, "views")
    }
}
