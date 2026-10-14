package com.acme.transport.lastmile.locker.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingLockerPolicyTest {
    @Test
    fun `starts in requested`() {
        val item = newRollingLockerPolicy("code-408", "title-959")
        assertEquals(RollingLockerPolicyStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRollingLockerPolicy("code-408", "title-959")
        try {
            item.moveTo(RollingLockerPolicyStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingLockerPolicyStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRollingLockerPolicy("code-408", "title-959")
        item.status = RollingLockerPolicyStatus.EXPIRED
        assertFails("move") { item.moveTo(RollingLockerPolicyStatus.REQUESTED) }
    }
}
