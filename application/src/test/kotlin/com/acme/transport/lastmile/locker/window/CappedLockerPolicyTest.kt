package com.acme.transport.lastmile.locker.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedLockerPolicyTest {
    @Test
    fun `starts in created`() {
        val item = newCappedLockerPolicy("code-742", "name-480")
        assertEquals(CappedLockerPolicyStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCappedLockerPolicy("code-742", "name-480")
        try {
            item.moveTo(CappedLockerPolicyStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedLockerPolicyStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCappedLockerPolicy("code-742", "name-480")
        item.status = CappedLockerPolicyStatus.RETIRED
        assertFails("move") { item.moveTo(CappedLockerPolicyStatus.CREATED) }
    }
}
