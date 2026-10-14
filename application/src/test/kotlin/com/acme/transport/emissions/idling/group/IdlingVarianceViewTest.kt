package com.acme.transport.emissions.idling.group

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingVariance("code-419", "name-32")
        val view = toIdlingVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingVarianceViews(mutableListOf<IdlingVariance>(item, item)).size, "views")
    }
}
