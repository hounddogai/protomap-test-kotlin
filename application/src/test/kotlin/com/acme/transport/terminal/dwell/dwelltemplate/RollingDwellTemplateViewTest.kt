package com.acme.transport.terminal.dwell.dwelltemplate

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingDwellTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingDwellTemplate("code-53", "name-52")
        val view = toRollingDwellTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingDwellTemplateViews(mutableListOf<RollingDwellTemplate>(item, item)).size, "views")
    }
}
