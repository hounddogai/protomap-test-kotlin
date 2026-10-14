package com.acme.transport.emissions.evcharge.chargebaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictEvChargeBaselineTest {
    @Test
    fun `starts in open`() {
        val item = newStrictEvChargeBaseline("code-847", "title-380")
        assertEquals(StrictEvChargeBaselineStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newStrictEvChargeBaseline("code-847", "title-380")
        try {
            item.moveTo(StrictEvChargeBaselineStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictEvChargeBaselineStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictEvChargeBaseline("code-847", "title-380")
        item.status = StrictEvChargeBaselineStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictEvChargeBaselineStatus.OPEN) }
    }
}
