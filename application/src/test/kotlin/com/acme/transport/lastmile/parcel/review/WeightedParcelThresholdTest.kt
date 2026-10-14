package com.acme.transport.lastmile.parcel.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedParcelThresholdTest {
    @Test
    fun `starts in draft`() {
        val item = newWeightedParcelThreshold("code-380", "title-750")
        assertEquals(WeightedParcelThresholdStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newWeightedParcelThreshold("code-380", "title-750")
        try {
            item.moveTo(WeightedParcelThresholdStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedParcelThresholdStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newWeightedParcelThreshold("code-380", "title-750")
        item.status = WeightedParcelThresholdStatus.CLOSED
        assertFails("move") { item.moveTo(WeightedParcelThresholdStatus.DRAFT) }
    }
}
