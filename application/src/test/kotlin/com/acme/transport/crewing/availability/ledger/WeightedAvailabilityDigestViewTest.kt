package com.acme.transport.crewing.availability.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedAvailabilityDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedAvailabilityDigest("code-414", "title-362")
        val view = toWeightedAvailabilityDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedAvailabilityDigestViews(mutableListOf<WeightedAvailabilityDigest>(item, item)).size, "views")
    }
}
