package com.acme.transport.emissions.emissionfactor.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedEmissionFactorDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedEmissionFactorDigest("code-681", "title-472")
        val view = toBatchedEmissionFactorDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedEmissionFactorDigestViews(mutableListOf<BatchedEmissionFactorDigest>(item, item)).size, "views")
    }
}
