package com.acme.transport.safety.violation.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingViolationProfileTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingViolationProfile("code-722", "title-720")
        assertEquals(RollingViolationProfileStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newRollingViolationProfile("code-722", "title-720")
        try {
            item.moveTo(RollingViolationProfileStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingViolationProfileStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingViolationProfile("code-722", "title-720")
        item.status = RollingViolationProfileStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingViolationProfileStatus.PROPOSED) }
    }
}
