package com.acme.transport.emissions.idling.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingRollup("code-14", "label-849")
        val view = toIdlingRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingRollupViews(mutableListOf<IdlingRollup>(item, item)).size, "views")
    }
}
