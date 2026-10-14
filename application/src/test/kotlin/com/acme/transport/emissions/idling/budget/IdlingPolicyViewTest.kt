package com.acme.transport.emissions.idling.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingPolicy("code-907", "title-439")
        val view = toIdlingPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingPolicyViews(mutableListOf<IdlingPolicy>(item, item)).size, "views")
    }
}
