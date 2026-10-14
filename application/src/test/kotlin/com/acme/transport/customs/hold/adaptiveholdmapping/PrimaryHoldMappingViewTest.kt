package com.acme.transport.customs.hold.adaptiveholdmapping

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryHoldMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryHoldMapping("code-267", "label-877")
        val view = toPrimaryHoldMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryHoldMappingViews(mutableListOf<PrimaryHoldMapping>(item, item)).size, "views")
    }
}
