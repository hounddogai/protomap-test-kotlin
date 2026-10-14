package com.acme.transport.emissions.fuelburn.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryFuelBurnNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryFuelBurnNotice("code-51", "name-823")
        val view = toPrimaryFuelBurnNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryFuelBurnNoticeViews(mutableListOf<PrimaryFuelBurnNotice>(item, item)).size, "views")
    }
}
