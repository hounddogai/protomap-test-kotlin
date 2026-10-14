package com.acme.transport.customs.broker.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalBrokerVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalBrokerVariance("code-763", "title-792")
        val view = toRegionalBrokerVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalBrokerVarianceViews(mutableListOf<RegionalBrokerVariance>(item, item)).size, "views")
    }
}
