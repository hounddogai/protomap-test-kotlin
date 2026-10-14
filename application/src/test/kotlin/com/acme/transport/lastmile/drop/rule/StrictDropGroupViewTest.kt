package com.acme.transport.lastmile.drop.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictDropGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictDropGroup("code-352", "title-126")
        val view = toStrictDropGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictDropGroupViews(mutableListOf<StrictDropGroup>(item, item)).size, "views")
    }
}
