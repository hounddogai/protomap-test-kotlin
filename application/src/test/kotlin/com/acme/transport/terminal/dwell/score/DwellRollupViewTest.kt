package com.acme.transport.terminal.dwell.score

import kotlin.test.Test
import kotlin.test.assertEquals

class DwellRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDwellRollup("code-180", "title-301")
        val view = toDwellRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDwellRollupViews(mutableListOf<DwellRollup>(item, item)).size, "views")
    }
}
