package com.acme.transport.lastmile.locker.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerPreferenceTest {
    @Test
    fun `starts in requested`() {
        val item = newLockerPreference("code-237", "name-341")
        assertEquals(LockerPreferenceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newLockerPreference("code-237", "name-341")
        try {
            item.moveTo(LockerPreferenceStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerPreferenceStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLockerPreference("code-237", "name-341")
        item.status = LockerPreferenceStatus.EXPIRED
        assertFails("move") { item.moveTo(LockerPreferenceStatus.REQUESTED) }
    }
}
