package com.acme.transport.linehaul.backhaul.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingBackhaulPolicyTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingBackhaulPolicy("code-111", "title-448")
        assertEquals(RollingBackhaulPolicyStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRollingBackhaulPolicy("code-111", "title-448")
        try {
            item.moveTo(RollingBackhaulPolicyStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingBackhaulPolicyStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingBackhaulPolicy("code-111", "title-448")
        item.status = RollingBackhaulPolicyStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingBackhaulPolicyStatus.PROPOSED) }
    }
}
