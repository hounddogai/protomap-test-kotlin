package com.acme.transport.freightpay.ratecard.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientRateCardPolicyTest {
    @Test
    fun `starts in open`() {
        val item = newLenientRateCardPolicy("code-710", "name-425")
        assertEquals(LenientRateCardPolicyStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newLenientRateCardPolicy("code-710", "name-425")
        try {
            item.moveTo(LenientRateCardPolicyStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientRateCardPolicyStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientRateCardPolicy("code-710", "name-425")
        item.status = LenientRateCardPolicyStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientRateCardPolicyStatus.OPEN) }
    }
}
