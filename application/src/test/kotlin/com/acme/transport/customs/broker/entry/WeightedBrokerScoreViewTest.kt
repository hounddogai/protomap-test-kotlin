package com.acme.transport.customs.broker.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedBrokerScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedBrokerScore("code-656", "label-104")
        val view = toWeightedBrokerScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedBrokerScoreViews(mutableListOf<WeightedBrokerScore>(item, item)).size, "views")
    }
}
