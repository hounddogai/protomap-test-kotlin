package com.acme.transport.emissions.emissionfactor.factorcycle

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedEmissionFactorSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedEmissionFactorSegment("code-265", "title-56")
        val view = toBatchedEmissionFactorSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedEmissionFactorSegmentViews(mutableListOf<BatchedEmissionFactorSegment>(item, item)).size, "views")
    }
}
