package com.acme.transport.safety.nearmiss.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualNearMissProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualNearMissProfile("code-684", "name-555")
        val view = toManualNearMissProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualNearMissProfileViews(mutableListOf<ManualNearMissProfile>(item, item)).size, "views")
    }
}
