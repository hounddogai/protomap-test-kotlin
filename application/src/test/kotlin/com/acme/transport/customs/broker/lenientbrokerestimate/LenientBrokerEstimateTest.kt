package com.acme.transport.customs.broker.lenientbrokerestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientBrokerEstimateTest {
    @Test
    fun `starts in requested`() {
        val item = newLenientBrokerEstimate("code-92", "title-884")
        assertEquals(LenientBrokerEstimateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLenientBrokerEstimate("code-92", "title-884")
        try {
            item.moveTo(LenientBrokerEstimateStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientBrokerEstimateStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLenientBrokerEstimate("code-92", "title-884")
        item.status = LenientBrokerEstimateStatus.EXPIRED
        assertFails("move") { item.moveTo(LenientBrokerEstimateStatus.REQUESTED) }
    }
}
