package com.acme.transport.customs.clearance.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryClearanceProfileTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryClearanceProfile("code-67", "name-113")
        assertEquals(PrimaryClearanceProfileStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newPrimaryClearanceProfile("code-67", "name-113")
        try {
            item.moveTo(PrimaryClearanceProfileStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryClearanceProfileStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryClearanceProfile("code-67", "name-113")
        item.status = PrimaryClearanceProfileStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryClearanceProfileStatus.REQUESTED) }
    }
}
