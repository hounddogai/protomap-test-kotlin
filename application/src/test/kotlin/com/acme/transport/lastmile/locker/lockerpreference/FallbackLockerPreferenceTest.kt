package com.acme.transport.lastmile.locker.lockerpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackLockerPreferenceTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackLockerPreference("code-229", "label-48")
        assertEquals(FallbackLockerPreferenceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newFallbackLockerPreference("code-229", "label-48")
        try {
            item.moveTo(FallbackLockerPreferenceStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackLockerPreferenceStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackLockerPreference("code-229", "label-48")
        item.status = FallbackLockerPreferenceStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackLockerPreferenceStatus.IDLE) }
    }
}
