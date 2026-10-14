package com.acme.transport.customs.broker.group

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalBrokerGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalBrokerGroup("code-182", "name-339")
        val view = toSeasonalBrokerGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalBrokerGroupViews(mutableListOf<SeasonalBrokerGroup>(item, item)).size, "views")
    }
}
