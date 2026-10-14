package com.acme.transport.customs.broker.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveBrokerTierTest {
    @Test
    fun `starts in open`() {
        val item = newAdaptiveBrokerTier("code-856", "name-701")
        assertEquals(AdaptiveBrokerTierStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newAdaptiveBrokerTier("code-856", "name-701")
        try {
            item.moveTo(AdaptiveBrokerTierStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveBrokerTierStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAdaptiveBrokerTier("code-856", "name-701")
        item.status = AdaptiveBrokerTierStatus.ARCHIVED
        assertFails("move") { item.moveTo(AdaptiveBrokerTierStatus.OPEN) }
    }
}
