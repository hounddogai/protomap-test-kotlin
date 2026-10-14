package com.acme.transport.linehaul.backhaul.request

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingBackhaulReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingBackhaulReview("code-35", "name-492")
        val view = toRollingBackhaulReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingBackhaulReviewViews(mutableListOf<RollingBackhaulReview>(item, item)).size, "views")
    }
}
