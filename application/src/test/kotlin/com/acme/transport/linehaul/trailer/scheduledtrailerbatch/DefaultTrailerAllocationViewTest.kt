package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultTrailerAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultTrailerAllocation("code-635", "name-642")
        val view = toDefaultTrailerAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultTrailerAllocationViews(mutableListOf<DefaultTrailerAllocation>(item, item)).size, "views")
    }
}
