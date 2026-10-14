package com.acme.transport.customs.broker.group

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultBrokerOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultBrokerOverride("code-778", "label-372")
        val view = toDefaultBrokerOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultBrokerOverrideViews(mutableListOf<DefaultBrokerOverride>(item, item)).size, "views")
    }
}
