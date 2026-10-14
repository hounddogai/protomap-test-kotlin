package com.acme.transport.freightpay.accessorial.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorialBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAccessorialBaseline("code-3", "title-431")
        val view = toAccessorialBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAccessorialBaselineViews(mutableListOf<AccessorialBaseline>(item, item)).size, "views")
    }
}
