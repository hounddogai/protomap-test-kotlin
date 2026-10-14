package com.acme.transport.network.servicearea.areatarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveServiceAreaTargetTest {
    @Test
    fun `starts in proposed`() {
        val item = newAdaptiveServiceAreaTarget("code-741", "label-736")
        assertEquals(AdaptiveServiceAreaTargetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newAdaptiveServiceAreaTarget("code-741", "label-736")
        try {
            item.moveTo(AdaptiveServiceAreaTargetStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveServiceAreaTargetStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAdaptiveServiceAreaTarget("code-741", "label-736")
        item.status = AdaptiveServiceAreaTargetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AdaptiveServiceAreaTargetStatus.PROPOSED) }
    }
}
