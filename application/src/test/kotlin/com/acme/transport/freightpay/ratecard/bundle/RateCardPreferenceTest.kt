package com.acme.transport.freightpay.ratecard.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardPreferenceTest {
    @Test
    fun `starts in requested`() {
        val item = newRateCardPreference("code-474", "label-345")
        assertEquals(RateCardPreferenceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRateCardPreference("code-474", "label-345")
        try {
            item.moveTo(RateCardPreferenceStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardPreferenceStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRateCardPreference("code-474", "label-345")
        item.status = RateCardPreferenceStatus.EXPIRED
        assertFails("move") { item.moveTo(RateCardPreferenceStatus.REQUESTED) }
    }
}
