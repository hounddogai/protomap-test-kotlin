package com.acme.transport.emissions.emissionfactor.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorReview("code-36", "name-885")
        val view = toEmissionFactorReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorReviewViews(mutableListOf<EmissionFactorReview>(item, item)).size, "views")
    }
}
