package com.acme.transport.crewing.roster.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterVariance("code-872", "label-875")
        val view = toRosterVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterVarianceViews(mutableListOf<RosterVariance>(item, item)).size, "views")
    }
}
