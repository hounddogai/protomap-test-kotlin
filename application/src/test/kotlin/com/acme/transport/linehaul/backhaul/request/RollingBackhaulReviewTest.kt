package com.acme.transport.linehaul.backhaul.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingBackhaulReviewTest {
    @Test
    fun `starts in new`() {
        val item = newRollingBackhaulReview("code-35", "name-492")
        assertEquals(RollingBackhaulReviewStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newRollingBackhaulReview("code-35", "name-492")
        try {
            item.moveTo(RollingBackhaulReviewStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingBackhaulReviewStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRollingBackhaulReview("code-35", "name-492")
        item.status = RollingBackhaulReviewStatus.FAILED
        assertFails("move") { item.moveTo(RollingBackhaulReviewStatus.NEW) }
    }
}
