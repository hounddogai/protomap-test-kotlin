package com.acme.transport.safety.coaching.coachingdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingCoachingDigestTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingCoachingDigest("code-286", "label-118")
        assertEquals(RollingCoachingDigestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newRollingCoachingDigest("code-286", "label-118")
        try {
            item.moveTo(RollingCoachingDigestStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingCoachingDigestStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingCoachingDigest("code-286", "label-118")
        item.status = RollingCoachingDigestStatus.STOPPED
        assertFails("move") { item.moveTo(RollingCoachingDigestStatus.IDLE) }
    }
}
