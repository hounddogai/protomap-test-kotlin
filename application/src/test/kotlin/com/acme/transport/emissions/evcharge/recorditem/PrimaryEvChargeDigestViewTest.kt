package com.acme.transport.emissions.evcharge.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryEvChargeDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryEvChargeDigest("code-869", "name-666")
        val view = toPrimaryEvChargeDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryEvChargeDigestViews(mutableListOf<PrimaryEvChargeDigest>(item, item)).size, "views")
    }
}
