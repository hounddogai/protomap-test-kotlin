package com.acme.transport.lastmile.locker.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalLockerRecordTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalLockerRecord("code-117", "name-161")
        assertEquals(GlobalLockerRecordStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newGlobalLockerRecord("code-117", "name-161")
        try {
            item.moveTo(GlobalLockerRecordStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalLockerRecordStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalLockerRecord("code-117", "name-161")
        item.status = GlobalLockerRecordStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalLockerRecordStatus.REQUESTED) }
    }
}
