package com.acme.transport.emissions.idling.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryIdlingBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryIdlingBundle("code-111", "label-317")
        val view = toPrimaryIdlingBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryIdlingBundleViews(mutableListOf<PrimaryIdlingBundle>(item, item)).size, "views")
    }
}
