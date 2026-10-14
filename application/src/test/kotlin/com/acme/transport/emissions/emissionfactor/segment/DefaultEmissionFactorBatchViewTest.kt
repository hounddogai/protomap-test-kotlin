package com.acme.transport.emissions.emissionfactor.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultEmissionFactorBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultEmissionFactorBatch("code-472", "label-756")
        val view = toDefaultEmissionFactorBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultEmissionFactorBatchViews(mutableListOf<DefaultEmissionFactorBatch>(item, item)).size, "views")
    }
}
