package com.acme.transport.customs.broker.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalBrokerOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalBrokerOverride("code-376", "title-431")
        val view = toGlobalBrokerOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalBrokerOverrideViews(mutableListOf<GlobalBrokerOverride>(item, item)).size, "views")
    }
}
