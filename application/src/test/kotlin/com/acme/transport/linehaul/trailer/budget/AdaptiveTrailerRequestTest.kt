package com.acme.transport.linehaul.trailer.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveTrailerRequestTest {
    @Test
    fun `starts in proposed`() {
        val item = newAdaptiveTrailerRequest("code-915", "name-803")
        assertEquals(AdaptiveTrailerRequestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newAdaptiveTrailerRequest("code-915", "name-803")
        try {
            item.moveTo(AdaptiveTrailerRequestStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveTrailerRequestStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAdaptiveTrailerRequest("code-915", "name-803")
        item.status = AdaptiveTrailerRequestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AdaptiveTrailerRequestStatus.PROPOSED) }
    }
}
