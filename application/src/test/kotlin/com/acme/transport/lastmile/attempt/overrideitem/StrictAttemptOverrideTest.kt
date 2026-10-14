package com.acme.transport.lastmile.attempt.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictAttemptOverrideTest {
    @Test
    fun `starts in open`() {
        val item = newStrictAttemptOverride("code-827", "label-466")
        assertEquals(StrictAttemptOverrideStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newStrictAttemptOverride("code-827", "label-466")
        try {
            item.moveTo(StrictAttemptOverrideStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictAttemptOverrideStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictAttemptOverride("code-827", "label-466")
        item.status = StrictAttemptOverrideStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictAttemptOverrideStatus.OPEN) }
    }
}
