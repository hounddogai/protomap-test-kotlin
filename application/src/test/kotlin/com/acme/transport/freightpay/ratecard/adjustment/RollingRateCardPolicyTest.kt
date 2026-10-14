package com.acme.transport.freightpay.ratecard.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingRateCardPolicyTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingRateCardPolicy("code-467", "name-607")
        assertEquals(RollingRateCardPolicyStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRollingRateCardPolicy("code-467", "name-607")
        try {
            item.moveTo(RollingRateCardPolicyStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingRateCardPolicyStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingRateCardPolicy("code-467", "name-607")
        item.status = RollingRateCardPolicyStatus.CLOSED
        assertFails("move") { item.moveTo(RollingRateCardPolicyStatus.DRAFT) }
    }
}
