package com.acme.transport.freightpay.dispute.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDisputeTierTest {
    @Test
    fun `starts in proposed`() {
        val item = newFallbackDisputeTier("code-873", "title-61")
        assertEquals(FallbackDisputeTierStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newFallbackDisputeTier("code-873", "title-61")
        try {
            item.moveTo(FallbackDisputeTierStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDisputeTierStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFallbackDisputeTier("code-873", "title-61")
        item.status = FallbackDisputeTierStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FallbackDisputeTierStatus.PROPOSED) }
    }
}
