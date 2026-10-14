package com.acme.transport.network.servicearea.score

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyServiceAreaScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyServiceAreaScore("code-501", "label-906")
        val view = toLegacyServiceAreaScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyServiceAreaScoreViews(mutableListOf<LegacyServiceAreaScore>(item, item)).size, "views")
    }
}
