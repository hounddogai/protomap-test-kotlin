package com.acme.transport.crewing.swap.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class SwapEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSwapEntry("code-713", "name-210")
        val view = toSwapEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSwapEntryViews(mutableListOf<SwapEntry>(item, item)).size, "views")
    }
}
