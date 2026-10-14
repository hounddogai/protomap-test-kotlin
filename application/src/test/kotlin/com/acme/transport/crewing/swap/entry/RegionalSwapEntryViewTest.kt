package com.acme.transport.crewing.swap.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalSwapEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalSwapEntry("code-506", "title-288")
        val view = toRegionalSwapEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalSwapEntryViews(mutableListOf<RegionalSwapEntry>(item, item)).size, "views")
    }
}
