package com.acme.transport.lastmile.locker.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackLockerBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackLockerBaseline("code-683", "name-510")
        val view = toFallbackLockerBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackLockerBaselineViews(mutableListOf<FallbackLockerBaseline>(item, item)).size, "views")
    }
}
