package com.acme.transport.freightpay.accessorial.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorialScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAccessorialScore("code-136", "label-146")
        val view = toAccessorialScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAccessorialScoreViews(mutableListOf<AccessorialScore>(item, item)).size, "views")
    }
}
