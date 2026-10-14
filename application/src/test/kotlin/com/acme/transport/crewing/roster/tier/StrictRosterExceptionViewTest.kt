package com.acme.transport.crewing.roster.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictRosterExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictRosterException("code-567", "label-242")
        val view = toStrictRosterExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictRosterExceptionViews(mutableListOf<StrictRosterException>(item, item)).size, "views")
    }
}
