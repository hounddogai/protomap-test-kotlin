package com.acme.transport.terminal.sortation.sortationbaseline

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalSortationOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalSortationOverride("code-825", "name-14")
        val view = toRegionalSortationOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalSortationOverrideViews(mutableListOf<RegionalSortationOverride>(item, item)).size, "views")
    }
}
