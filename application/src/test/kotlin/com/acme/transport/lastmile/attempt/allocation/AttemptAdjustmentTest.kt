package com.acme.transport.lastmile.attempt.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptAdjustmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newAttemptAdjustment("code-641", "label-714")
        assertEquals(AttemptAdjustmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newAttemptAdjustment("code-641", "label-714")
        try {
            item.moveTo(AttemptAdjustmentStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptAdjustmentStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAttemptAdjustment("code-641", "label-714")
        item.status = AttemptAdjustmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AttemptAdjustmentStatus.PROPOSED) }
    }
}
