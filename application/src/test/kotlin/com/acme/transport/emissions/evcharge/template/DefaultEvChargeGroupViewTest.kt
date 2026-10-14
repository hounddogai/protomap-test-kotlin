package com.acme.transport.emissions.evcharge.template

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultEvChargeGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultEvChargeGroup("code-11", "title-251")
        val view = toDefaultEvChargeGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultEvChargeGroupViews(mutableListOf<DefaultEvChargeGroup>(item, item)).size, "views")
    }
}
