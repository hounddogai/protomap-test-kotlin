package com.acme.transport.terminal.dwell.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalDwellProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalDwellProfile("code-99", "title-267")
        val view = toRegionalDwellProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalDwellProfileViews(mutableListOf<RegionalDwellProfile>(item, item)).size, "views")
    }
}
