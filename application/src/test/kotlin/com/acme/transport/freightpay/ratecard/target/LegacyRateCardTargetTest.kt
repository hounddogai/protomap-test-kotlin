package com.acme.transport.freightpay.ratecard.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyRateCardTargetTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyRateCardTarget("code-322", "label-366")
        assertEquals(LegacyRateCardTargetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newLegacyRateCardTarget("code-322", "label-366")
        try {
            item.moveTo(LegacyRateCardTargetStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyRateCardTargetStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyRateCardTarget("code-322", "label-366")
        item.status = LegacyRateCardTargetStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyRateCardTargetStatus.IDLE) }
    }
}
