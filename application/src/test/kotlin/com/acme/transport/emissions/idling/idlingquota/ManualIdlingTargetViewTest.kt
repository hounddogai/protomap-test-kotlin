package com.acme.transport.emissions.idling.idlingquota

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualIdlingTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualIdlingTarget("code-695", "title-529")
        val view = toManualIdlingTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualIdlingTargetViews(mutableListOf<ManualIdlingTarget>(item, item)).size, "views")
    }
}
