package com.acme.transport.crewing.restbreak.breakmapping

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultRestBreakVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultRestBreakVariance("code-438", "title-642")
        val view = toDefaultRestBreakVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultRestBreakVarianceViews(mutableListOf<DefaultRestBreakVariance>(item, item)).size, "views")
    }
}
