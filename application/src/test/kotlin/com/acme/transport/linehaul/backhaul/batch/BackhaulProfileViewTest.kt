package com.acme.transport.linehaul.backhaul.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class BackhaulProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBackhaulProfile("code-18", "label-974")
        val view = toBackhaulProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBackhaulProfileViews(mutableListOf<BackhaulProfile>(item, item)).size, "views")
    }
}
