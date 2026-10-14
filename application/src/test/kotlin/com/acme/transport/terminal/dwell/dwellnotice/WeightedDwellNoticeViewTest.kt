package com.acme.transport.terminal.dwell.dwellnotice

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedDwellNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedDwellNotice("code-679", "title-648")
        val view = toWeightedDwellNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedDwellNoticeViews(mutableListOf<WeightedDwellNotice>(item, item)).size, "views")
    }
}
