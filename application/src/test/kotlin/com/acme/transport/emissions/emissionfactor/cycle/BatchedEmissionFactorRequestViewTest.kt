package com.acme.transport.emissions.emissionfactor.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedEmissionFactorRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedEmissionFactorRequest("code-917", "title-874")
        val view = toBatchedEmissionFactorRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedEmissionFactorRequestViews(mutableListOf<BatchedEmissionFactorRequest>(item, item)).size, "views")
    }
}
