package com.acme.transport.linehaul.backhaul.target

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedBackhaulPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedBackhaulPolicy("code-425", "title-371")
        val view = toCappedBackhaulPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedBackhaulPolicyViews(mutableListOf<CappedBackhaulPolicy>(item, item)).size, "views")
    }
}
