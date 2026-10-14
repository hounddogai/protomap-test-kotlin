package com.acme.transport.emissions.idling.idlingmetric

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingOverride("code-224", "title-643")
        val view = toIdlingOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingOverrideViews(mutableListOf<IdlingOverride>(item, item)).size, "views")
    }
}
