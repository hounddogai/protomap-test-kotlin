package com.acme.transport.linehaul.backhaul.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryBackhaulScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryBackhaulScore("code-926", "title-841")
        val view = toPrimaryBackhaulScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryBackhaulScoreViews(mutableListOf<PrimaryBackhaulScore>(item, item)).size, "views")
    }
}
