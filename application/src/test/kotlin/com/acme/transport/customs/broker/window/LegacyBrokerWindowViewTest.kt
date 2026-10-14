package com.acme.transport.customs.broker.window

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyBrokerWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyBrokerWindow("code-814", "name-636")
        val view = toLegacyBrokerWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyBrokerWindowViews(mutableListOf<LegacyBrokerWindow>(item, item)).size, "views")
    }
}
