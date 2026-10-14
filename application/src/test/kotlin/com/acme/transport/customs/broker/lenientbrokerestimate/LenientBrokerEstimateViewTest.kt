package com.acme.transport.customs.broker.lenientbrokerestimate

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientBrokerEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientBrokerEstimate("code-92", "title-884")
        val view = toLenientBrokerEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientBrokerEstimateViews(mutableListOf<LenientBrokerEstimate>(item, item)).size, "views")
    }
}
