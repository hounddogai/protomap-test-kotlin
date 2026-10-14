package com.acme.transport.freightpay.ratecard.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveRateCardBaselineTest {
    @Test
    fun `starts in open`() {
        val item = newAdaptiveRateCardBaseline("code-804", "name-18")
        assertEquals(AdaptiveRateCardBaselineStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newAdaptiveRateCardBaseline("code-804", "name-18")
        try {
            item.moveTo(AdaptiveRateCardBaselineStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveRateCardBaselineStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAdaptiveRateCardBaseline("code-804", "name-18")
        item.status = AdaptiveRateCardBaselineStatus.ARCHIVED
        assertFails("move") { item.moveTo(AdaptiveRateCardBaselineStatus.OPEN) }
    }
}
