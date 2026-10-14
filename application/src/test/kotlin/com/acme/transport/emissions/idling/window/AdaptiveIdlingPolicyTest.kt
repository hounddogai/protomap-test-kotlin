package com.acme.transport.emissions.idling.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveIdlingPolicyTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveIdlingPolicy("code-625", "title-664")
        assertEquals(AdaptiveIdlingPolicyStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newAdaptiveIdlingPolicy("code-625", "title-664")
        try {
            item.moveTo(AdaptiveIdlingPolicyStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveIdlingPolicyStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveIdlingPolicy("code-625", "title-664")
        item.status = AdaptiveIdlingPolicyStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveIdlingPolicyStatus.PLANNED) }
    }
}
