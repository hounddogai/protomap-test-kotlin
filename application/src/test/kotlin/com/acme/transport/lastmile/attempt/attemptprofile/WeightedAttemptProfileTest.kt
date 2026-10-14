package com.acme.transport.lastmile.attempt.attemptprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedAttemptProfileTest {
    @Test
    fun `starts in created`() {
        val item = newWeightedAttemptProfile("code-488", "title-737")
        assertEquals(WeightedAttemptProfileStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newWeightedAttemptProfile("code-488", "title-737")
        try {
            item.moveTo(WeightedAttemptProfileStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedAttemptProfileStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newWeightedAttemptProfile("code-488", "title-737")
        item.status = WeightedAttemptProfileStatus.RETIRED
        assertFails("move") { item.moveTo(WeightedAttemptProfileStatus.CREATED) }
    }
}
