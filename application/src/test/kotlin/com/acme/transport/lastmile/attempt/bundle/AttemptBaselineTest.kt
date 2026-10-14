package com.acme.transport.lastmile.attempt.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptBaselineTest {
    @Test
    fun `starts in created`() {
        val item = newAttemptBaseline("code-551", "title-262")
        assertEquals(AttemptBaselineStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newAttemptBaseline("code-551", "title-262")
        try {
            item.moveTo(AttemptBaselineStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptBaselineStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAttemptBaseline("code-551", "title-262")
        item.status = AttemptBaselineStatus.RETIRED
        assertFails("move") { item.moveTo(AttemptBaselineStatus.CREATED) }
    }
}
