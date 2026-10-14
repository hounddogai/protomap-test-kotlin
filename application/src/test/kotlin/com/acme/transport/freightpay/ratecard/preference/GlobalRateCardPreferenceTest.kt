package com.acme.transport.freightpay.ratecard.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalRateCardPreferenceTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalRateCardPreference("code-910", "name-591")
        assertEquals(GlobalRateCardPreferenceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newGlobalRateCardPreference("code-910", "name-591")
        try {
            item.moveTo(GlobalRateCardPreferenceStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalRateCardPreferenceStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalRateCardPreference("code-910", "name-591")
        item.status = GlobalRateCardPreferenceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalRateCardPreferenceStatus.PROPOSED) }
    }
}
