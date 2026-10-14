package com.acme.transport.customs.hold.adaptiveholdmapping

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalHoldPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalHoldPolicy("code-562", "name-767")
        val view = toGlobalHoldPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalHoldPolicyViews(mutableListOf<GlobalHoldPolicy>(item, item)).size, "views")
    }
}
