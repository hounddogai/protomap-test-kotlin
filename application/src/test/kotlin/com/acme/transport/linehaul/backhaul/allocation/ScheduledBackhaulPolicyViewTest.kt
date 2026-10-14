package com.acme.transport.linehaul.backhaul.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledBackhaulPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledBackhaulPolicy("code-412", "title-280")
        val view = toScheduledBackhaulPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledBackhaulPolicyViews(mutableListOf<ScheduledBackhaulPolicy>(item, item)).size, "views")
    }
}
