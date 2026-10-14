package com.acme.transport.terminal.sortation.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class SortationVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSortationVariance("code-651", "name-876")
        val view = toSortationVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSortationVarianceViews(mutableListOf<SortationVariance>(item, item)).size, "views")
    }
}
