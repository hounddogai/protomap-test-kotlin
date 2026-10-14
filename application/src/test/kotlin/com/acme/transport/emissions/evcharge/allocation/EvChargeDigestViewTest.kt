package com.acme.transport.emissions.evcharge.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class EvChargeDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEvChargeDigest("code-382", "name-668")
        val view = toEvChargeDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEvChargeDigestViews(mutableListOf<EvChargeDigest>(item, item)).size, "views")
    }
}
