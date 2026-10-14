package com.acme.transport.safety.coaching.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyCoachingWindowTest {
    @Test
    fun `starts in requested`() {
        val item = newLegacyCoachingWindow("code-919", "label-816")
        assertEquals(LegacyCoachingWindowStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLegacyCoachingWindow("code-919", "label-816")
        try {
            item.moveTo(LegacyCoachingWindowStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyCoachingWindowStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLegacyCoachingWindow("code-919", "label-816")
        item.status = LegacyCoachingWindowStatus.EXPIRED
        assertFails("move") { item.moveTo(LegacyCoachingWindowStatus.REQUESTED) }
    }
}
