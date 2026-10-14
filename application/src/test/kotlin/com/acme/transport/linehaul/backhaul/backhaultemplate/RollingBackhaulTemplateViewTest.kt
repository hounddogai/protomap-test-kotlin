package com.acme.transport.linehaul.backhaul.backhaultemplate

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingBackhaulTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingBackhaulTemplate("code-26", "name-803")
        val view = toRollingBackhaulTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingBackhaulTemplateViews(mutableListOf<RollingBackhaulTemplate>(item, item)).size, "views")
    }
}
