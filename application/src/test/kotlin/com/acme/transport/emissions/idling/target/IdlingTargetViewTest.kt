package com.acme.transport.emissions.idling.target

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingTarget("code-661", "title-769")
        val view = toIdlingTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingTargetViews(mutableListOf<IdlingTarget>(item, item)).size, "views")
    }
}
