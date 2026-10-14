package com.acme.transport.lastmile.locker.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class LockerVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLockerVariance("code-559", "title-268")
        val view = toLockerVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLockerVarianceViews(mutableListOf<LockerVariance>(item, item)).size, "views")
    }
}
