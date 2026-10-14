package com.acme.transport.customs.broker.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultBrokerDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultBrokerDigest("code-360", "title-476")
        val view = toDefaultBrokerDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultBrokerDigestViews(mutableListOf<DefaultBrokerDigest>(item, item)).size, "views")
    }
}
