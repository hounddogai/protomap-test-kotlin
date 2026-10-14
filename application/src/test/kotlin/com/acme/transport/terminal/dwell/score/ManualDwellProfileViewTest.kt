package com.acme.transport.terminal.dwell.score

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualDwellProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualDwellProfile("code-437", "title-133")
        val view = toManualDwellProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualDwellProfileViews(mutableListOf<ManualDwellProfile>(item, item)).size, "views")
    }
}
