package com.acme.transport.emissions.idling.idlingcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultIdlingRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultIdlingRollup("code-450", "title-438")
        val view = toDefaultIdlingRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultIdlingRollupViews(mutableListOf<DefaultIdlingRollup>(item, item)).size, "views")
    }
}
