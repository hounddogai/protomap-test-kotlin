package com.acme.transport.freightpay.ratecard.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRateCardTargetTest {
    @Test
    fun `starts in created`() {
        val item = newStrictRateCardTarget("code-439", "title-316")
        assertEquals(StrictRateCardTargetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newStrictRateCardTarget("code-439", "title-316")
        try {
            item.moveTo(StrictRateCardTargetStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRateCardTargetStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newStrictRateCardTarget("code-439", "title-316")
        item.status = StrictRateCardTargetStatus.RETIRED
        assertFails("move") { item.moveTo(StrictRateCardTargetStatus.CREATED) }
    }
}
