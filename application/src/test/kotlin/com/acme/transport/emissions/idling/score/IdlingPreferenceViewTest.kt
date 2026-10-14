package com.acme.transport.emissions.idling.score

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingPreference("code-745", "title-357")
        val view = toIdlingPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingPreferenceViews(mutableListOf<IdlingPreference>(item, item)).size, "views")
    }
}
