package com.acme.transport.emissions.idling.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingAllocation("code-663", "label-747")
        val view = toIdlingAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingAllocationViews(mutableListOf<IdlingAllocation>(item, item)).size, "views")
    }
}
