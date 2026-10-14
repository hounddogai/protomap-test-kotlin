package com.acme.transport.crewing.restbreak.regionalbreakpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledRestBreakDigestTest {
    @Test
    fun `starts in planned`() {
        val item = newScheduledRestBreakDigest("code-199", "title-306")
        assertEquals(ScheduledRestBreakDigestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newScheduledRestBreakDigest("code-199", "title-306")
        try {
            item.moveTo(ScheduledRestBreakDigestStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledRestBreakDigestStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScheduledRestBreakDigest("code-199", "title-306")
        item.status = ScheduledRestBreakDigestStatus.CANCELLED
        assertFails("move") { item.moveTo(ScheduledRestBreakDigestStatus.PLANNED) }
    }
}
