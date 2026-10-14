package com.acme.transport.terminal.dwell.template

import kotlin.test.Test
import kotlin.test.assertEquals

class DwellProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDwellProfile("code-275", "title-39")
        val view = toDwellProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDwellProfileViews(mutableListOf<DwellProfile>(item, item)).size, "views")
    }
}
