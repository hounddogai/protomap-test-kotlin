package com.acme.transport.safety.coaching.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackCoachingWindowTest {
    @Test
    fun `starts in proposed`() {
        val item = newFallbackCoachingWindow("code-436", "title-981")
        assertEquals(FallbackCoachingWindowStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newFallbackCoachingWindow("code-436", "title-981")
        try {
            item.moveTo(FallbackCoachingWindowStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackCoachingWindowStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFallbackCoachingWindow("code-436", "title-981")
        item.status = FallbackCoachingWindowStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FallbackCoachingWindowStatus.PROPOSED) }
    }
}
