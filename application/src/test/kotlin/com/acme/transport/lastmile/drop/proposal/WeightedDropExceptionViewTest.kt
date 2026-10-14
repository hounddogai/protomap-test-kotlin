package com.acme.transport.lastmile.drop.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedDropExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedDropException("code-195", "title-495")
        val view = toWeightedDropExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedDropExceptionViews(mutableListOf<WeightedDropException>(item, item)).size, "views")
    }
}
