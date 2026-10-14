package com.acme.transport.freightpay.dispute.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class DisputeDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDisputeDigest("code-540", "title-579")
        val view = toDisputeDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDisputeDigestViews(mutableListOf<DisputeDigest>(item, item)).size, "views")
    }
}
