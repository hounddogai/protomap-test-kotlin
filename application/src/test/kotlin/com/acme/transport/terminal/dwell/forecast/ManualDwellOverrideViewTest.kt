package com.acme.transport.terminal.dwell.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualDwellOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualDwellOverride("code-695", "name-99")
        val view = toManualDwellOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualDwellOverrideViews(mutableListOf<ManualDwellOverride>(item, item)).size, "views")
    }
}
