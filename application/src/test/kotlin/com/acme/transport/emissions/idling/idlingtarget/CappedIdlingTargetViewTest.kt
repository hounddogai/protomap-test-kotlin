package com.acme.transport.emissions.idling.idlingtarget

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedIdlingTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedIdlingTarget("code-705", "label-605")
        val view = toCappedIdlingTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedIdlingTargetViews(mutableListOf<CappedIdlingTarget>(item, item)).size, "views")
    }
}
