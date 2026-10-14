package com.acme.transport.emissions.fuelburn.review

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalFuelBurnDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalFuelBurnDigest("code-402", "label-596")
        val view = toGlobalFuelBurnDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalFuelBurnDigestViews(mutableListOf<GlobalFuelBurnDigest>(item, item)).size, "views")
    }
}
