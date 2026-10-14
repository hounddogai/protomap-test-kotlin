package com.acme.transport.lastmile.locker.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictLockerTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictLockerTarget("code-294", "title-993")
        val view = toStrictLockerTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictLockerTargetViews(mutableListOf<StrictLockerTarget>(item, item)).size, "views")
    }
}
