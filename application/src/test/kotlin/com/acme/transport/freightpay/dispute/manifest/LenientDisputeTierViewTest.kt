package com.acme.transport.freightpay.dispute.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientDisputeTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientDisputeTier("code-546", "title-404")
        val view = toLenientDisputeTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientDisputeTierViews(mutableListOf<LenientDisputeTier>(item, item)).size, "views")
    }
}
