package com.acme.transport.lastmile.drop.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultDropPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultDropPreference("code-88", "title-315")
        val view = toDefaultDropPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultDropPreferenceViews(mutableListOf<DefaultDropPreference>(item, item)).size, "views")
    }
}
