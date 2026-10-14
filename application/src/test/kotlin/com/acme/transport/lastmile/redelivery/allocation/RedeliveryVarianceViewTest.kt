package com.acme.transport.lastmile.redelivery.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliveryVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRedeliveryVariance("code-631", "title-888")
        val view = toRedeliveryVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRedeliveryVarianceViews(mutableListOf<RedeliveryVariance>(item, item)).size, "views")
    }
}
