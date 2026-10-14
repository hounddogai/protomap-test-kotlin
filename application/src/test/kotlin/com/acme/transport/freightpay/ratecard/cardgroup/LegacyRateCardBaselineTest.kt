package com.acme.transport.freightpay.ratecard.cardgroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyRateCardBaselineTest {
    @Test
    fun `starts in created`() {
        val item = newLegacyRateCardBaseline("code-474", "name-421")
        assertEquals(LegacyRateCardBaselineStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLegacyRateCardBaseline("code-474", "name-421")
        try {
            item.moveTo(LegacyRateCardBaselineStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyRateCardBaselineStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLegacyRateCardBaseline("code-474", "name-421")
        item.status = LegacyRateCardBaselineStatus.RETIRED
        assertFails("move") { item.moveTo(LegacyRateCardBaselineStatus.CREATED) }
    }
}
