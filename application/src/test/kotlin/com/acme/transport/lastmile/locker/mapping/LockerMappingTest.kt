package com.acme.transport.lastmile.locker.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerMappingTest {
    @Test
    fun `starts in idle`() {
        val item = newLockerMapping("code-565", "label-298")
        assertEquals(LockerMappingStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newLockerMapping("code-565", "label-298")
        try {
            item.moveTo(LockerMappingStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerMappingStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLockerMapping("code-565", "label-298")
        item.status = LockerMappingStatus.STOPPED
        assertFails("move") { item.moveTo(LockerMappingStatus.IDLE) }
    }
}
