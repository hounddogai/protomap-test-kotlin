package com.acme.transport.customs.hold.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveHoldLedgerTest {
    @Test
    fun `starts in created`() {
        val item = newAdaptiveHoldLedger("code-843", "label-150")
        assertEquals(AdaptiveHoldLedgerStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newAdaptiveHoldLedger("code-843", "label-150")
        try {
            item.moveTo(AdaptiveHoldLedgerStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveHoldLedgerStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAdaptiveHoldLedger("code-843", "label-150")
        item.status = AdaptiveHoldLedgerStatus.RETIRED
        assertFails("move") { item.moveTo(AdaptiveHoldLedgerStatus.CREATED) }
    }
}
