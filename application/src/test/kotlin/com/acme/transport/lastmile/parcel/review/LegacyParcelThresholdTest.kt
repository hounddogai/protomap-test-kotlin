package com.acme.transport.lastmile.parcel.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyParcelThresholdTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyParcelThreshold("code-91", "label-153")
        assertEquals(LegacyParcelThresholdStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newLegacyParcelThreshold("code-91", "label-153")
        try {
            item.moveTo(LegacyParcelThresholdStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyParcelThresholdStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyParcelThreshold("code-91", "label-153")
        item.status = LegacyParcelThresholdStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyParcelThresholdStatus.PLANNED) }
    }
}
