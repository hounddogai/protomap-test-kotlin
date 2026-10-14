package com.acme.transport.lastmile.locker.lockerclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerBundleTest {
    @Test
    fun `starts in requested`() {
        val item = newLockerBundle("code-804", "name-765")
        assertEquals(LockerBundleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newLockerBundle("code-804", "name-765")
        try {
            item.moveTo(LockerBundleStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerBundleStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLockerBundle("code-804", "name-765")
        item.status = LockerBundleStatus.EXPIRED
        assertFails("move") { item.moveTo(LockerBundleStatus.REQUESTED) }
    }
}
