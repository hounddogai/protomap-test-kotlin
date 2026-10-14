package com.acme.transport.freightpay.ratecard.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalRateCardDigestTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalRateCardDigest("code-591", "label-14")
        assertEquals(SeasonalRateCardDigestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newSeasonalRateCardDigest("code-591", "label-14")
        try {
            item.moveTo(SeasonalRateCardDigestStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalRateCardDigestStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalRateCardDigest("code-591", "label-14")
        item.status = SeasonalRateCardDigestStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalRateCardDigestStatus.REQUESTED) }
    }
}
