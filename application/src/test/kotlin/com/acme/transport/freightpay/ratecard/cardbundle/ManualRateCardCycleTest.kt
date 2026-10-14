package com.acme.transport.freightpay.ratecard.cardbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualRateCardCycleTest {
    @Test
    fun `starts in requested`() {
        val item = newManualRateCardCycle("code-403", "label-425")
        assertEquals(ManualRateCardCycleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newManualRateCardCycle("code-403", "label-425")
        try {
            item.moveTo(ManualRateCardCycleStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualRateCardCycleStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newManualRateCardCycle("code-403", "label-425")
        item.status = ManualRateCardCycleStatus.EXPIRED
        assertFails("move") { item.moveTo(ManualRateCardCycleStatus.REQUESTED) }
    }
}
