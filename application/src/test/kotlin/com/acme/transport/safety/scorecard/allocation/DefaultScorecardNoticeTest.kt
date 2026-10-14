package com.acme.transport.safety.scorecard.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultScorecardNoticeTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefaultScorecardNotice("code-616", "title-169")
        assertEquals(DefaultScorecardNoticeStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDefaultScorecardNotice("code-616", "title-169")
        try {
            item.moveTo(DefaultScorecardNoticeStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultScorecardNoticeStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefaultScorecardNotice("code-616", "title-169")
        item.status = DefaultScorecardNoticeStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefaultScorecardNoticeStatus.PROPOSED) }
    }
}
