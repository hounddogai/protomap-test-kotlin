package com.acme.transport.lastmile.locker.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryLockerTargetTest {
    @Test
    fun `starts in idle`() {
        val item = newPrimaryLockerTarget("code-196", "label-517")
        assertEquals(PrimaryLockerTargetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newPrimaryLockerTarget("code-196", "label-517")
        try {
            item.moveTo(PrimaryLockerTargetStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryLockerTargetStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPrimaryLockerTarget("code-196", "label-517")
        item.status = PrimaryLockerTargetStatus.STOPPED
        assertFails("move") { item.moveTo(PrimaryLockerTargetStatus.IDLE) }
    }
}
