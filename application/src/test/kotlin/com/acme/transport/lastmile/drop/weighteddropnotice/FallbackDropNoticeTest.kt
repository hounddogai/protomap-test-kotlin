package com.acme.transport.lastmile.drop.weighteddropnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDropNoticeTest {
    @Test
    fun `starts in proposed`() {
        val item = newFallbackDropNotice("code-79", "name-238")
        assertEquals(FallbackDropNoticeStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newFallbackDropNotice("code-79", "name-238")
        try {
            item.moveTo(FallbackDropNoticeStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDropNoticeStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFallbackDropNotice("code-79", "name-238")
        item.status = FallbackDropNoticeStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FallbackDropNoticeStatus.PROPOSED) }
    }
}
