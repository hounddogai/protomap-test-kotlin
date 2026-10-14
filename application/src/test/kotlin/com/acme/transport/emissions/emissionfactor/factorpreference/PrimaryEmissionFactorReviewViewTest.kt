package com.acme.transport.emissions.emissionfactor.factorpreference

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryEmissionFactorReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryEmissionFactorReview("code-102", "label-735")
        val view = toPrimaryEmissionFactorReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryEmissionFactorReviewViews(mutableListOf<PrimaryEmissionFactorReview>(item, item)).size, "views")
    }
}
