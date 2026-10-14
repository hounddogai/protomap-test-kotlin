package com.acme.transport.network.servicearea.cappedareaassignment

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryServiceAreaTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryServiceAreaTarget("code-587", "title-640")
        val view = toPrimaryServiceAreaTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryServiceAreaTargetViews(mutableListOf<PrimaryServiceAreaTarget>(item, item)).size, "views")
    }
}
