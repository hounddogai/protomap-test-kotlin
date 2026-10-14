package com.acme.transport.emissions.idling.idlingforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingProfile("code-805", "label-368")
        val view = toIdlingProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingProfileViews(mutableListOf<IdlingProfile>(item, item)).size, "views")
    }
}
