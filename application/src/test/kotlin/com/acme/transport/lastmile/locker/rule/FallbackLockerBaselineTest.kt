package com.acme.transport.lastmile.locker.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackLockerBaselineTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackLockerBaseline("code-683", "name-510")
        assertEquals(FallbackLockerBaselineStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newFallbackLockerBaseline("code-683", "name-510")
        try {
            item.moveTo(FallbackLockerBaselineStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackLockerBaselineStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackLockerBaseline("code-683", "name-510")
        item.status = FallbackLockerBaselineStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackLockerBaselineStatus.IDLE) }
    }
}
