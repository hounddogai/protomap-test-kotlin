package com.acme.transport.customs.hold.holdmapping

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualHoldProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualHoldProfile("code-914", "label-216")
        val view = toManualHoldProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualHoldProfileViews(mutableListOf<ManualHoldProfile>(item, item)).size, "views")
    }
}
