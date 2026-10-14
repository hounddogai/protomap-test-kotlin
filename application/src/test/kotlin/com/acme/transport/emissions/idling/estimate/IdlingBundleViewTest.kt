package com.acme.transport.emissions.idling.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingBundle("code-200", "label-375")
        val view = toIdlingBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingBundleViews(mutableListOf<IdlingBundle>(item, item)).size, "views")
    }
}
