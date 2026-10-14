package com.acme.transport.freightpay.ratecard.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardAgreementTest {
    @Test
    fun `starts in idle`() {
        val item = newRateCardAgreement("code-17", "name-238")
        assertEquals(RateCardAgreementStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newRateCardAgreement("code-17", "name-238")
        try {
            item.moveTo(RateCardAgreementStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardAgreementStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRateCardAgreement("code-17", "name-238")
        item.status = RateCardAgreementStatus.STOPPED
        assertFails("move") { item.moveTo(RateCardAgreementStatus.IDLE) }
    }
}
