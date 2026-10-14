package com.acme.transport.lastmile.parcel.score

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyParcelScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyParcelScore("code-815", "title-497")
        val view = toLegacyParcelScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyParcelScoreViews(mutableListOf<LegacyParcelScore>(item, item)).size, "views")
    }
}
