package com.acme.transport.lastmile.locker.score

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedLockerScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedLockerScore("code-549", "label-591")
        val view = toCappedLockerScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedLockerScoreViews(mutableListOf<CappedLockerScore>(item, item)).size, "views")
    }
}
