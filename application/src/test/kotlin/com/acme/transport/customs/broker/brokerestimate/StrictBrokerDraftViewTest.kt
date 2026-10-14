package com.acme.transport.customs.broker.brokerestimate

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictBrokerDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictBrokerDraft("code-955", "label-769")
        val view = toStrictBrokerDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictBrokerDraftViews(mutableListOf<StrictBrokerDraft>(item, item)).size, "views")
    }
}
