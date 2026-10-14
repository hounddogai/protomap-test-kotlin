package com.acme.transport.emissions.emissionfactor.request

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedEmissionFactorBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedEmissionFactorBatch("code-191", "name-991")
        val view = toBatchedEmissionFactorBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedEmissionFactorBatchViews(mutableListOf<BatchedEmissionFactorBatch>(item, item)).size, "views")
    }
}
