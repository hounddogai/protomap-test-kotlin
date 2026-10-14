package com.acme.transport.linehaul.tender.weightedtenderrollup

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingTenderRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingTenderRollup("code-860", "title-414")
        val view = toRollingTenderRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingTenderRollupViews(mutableListOf<RollingTenderRollup>(item, item)).size, "views")
    }
}
