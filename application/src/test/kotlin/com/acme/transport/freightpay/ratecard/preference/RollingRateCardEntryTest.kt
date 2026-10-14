package com.acme.transport.freightpay.ratecard.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingRateCardEntryTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingRateCardEntry("code-238", "title-583")
        assertEquals(RollingRateCardEntryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newRollingRateCardEntry("code-238", "title-583")
        try {
            item.moveTo(RollingRateCardEntryStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingRateCardEntryStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingRateCardEntry("code-238", "title-583")
        item.status = RollingRateCardEntryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingRateCardEntryStatus.PROPOSED) }
    }
}
