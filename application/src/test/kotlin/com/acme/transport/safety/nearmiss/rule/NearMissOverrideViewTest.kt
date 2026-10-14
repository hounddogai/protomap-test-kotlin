package com.acme.transport.safety.nearmiss.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class NearMissOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNearMissOverride("code-184", "label-911")
        val view = toNearMissOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNearMissOverrideViews(mutableListOf<NearMissOverride>(item, item)).size, "views")
    }
}
