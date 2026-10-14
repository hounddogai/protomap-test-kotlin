package com.acme.transport.customs.broker.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerManifest("code-945", "name-905")
        val view = toBrokerManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerManifestViews(mutableListOf<BrokerManifest>(item, item)).size, "views")
    }
}
