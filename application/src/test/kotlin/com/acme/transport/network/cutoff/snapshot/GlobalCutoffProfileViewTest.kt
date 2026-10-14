package com.acme.transport.network.cutoff.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalCutoffProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalCutoffProfile("code-331", "title-489")
        val view = toGlobalCutoffProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalCutoffProfileViews(mutableListOf<GlobalCutoffProfile>(item, item)).size, "views")
    }
}
