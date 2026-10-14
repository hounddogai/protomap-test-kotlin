package com.acme.transport.lastmile.locker.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerPolicyTest {
    @Test
    fun `starts in created`() {
        val item = newLockerPolicy("code-77", "title-108")
        assertEquals(LockerPolicyStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLockerPolicy("code-77", "title-108")
        try {
            item.moveTo(LockerPolicyStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerPolicyStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLockerPolicy("code-77", "title-108")
        item.status = LockerPolicyStatus.RETIRED
        assertFails("move") { item.moveTo(LockerPolicyStatus.CREATED) }
    }
}
