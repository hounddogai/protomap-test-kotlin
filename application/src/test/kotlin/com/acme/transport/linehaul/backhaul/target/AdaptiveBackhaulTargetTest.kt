package com.acme.transport.linehaul.backhaul.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveBackhaulTargetTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveBackhaulTarget("code-292", "label-103")
        assertEquals(AdaptiveBackhaulTargetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newAdaptiveBackhaulTarget("code-292", "label-103")
        try {
            item.moveTo(AdaptiveBackhaulTargetStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveBackhaulTargetStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveBackhaulTarget("code-292", "label-103")
        item.status = AdaptiveBackhaulTargetStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveBackhaulTargetStatus.PLANNED) }
    }
}
