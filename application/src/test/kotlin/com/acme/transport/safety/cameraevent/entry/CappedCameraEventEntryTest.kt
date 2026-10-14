package com.acme.transport.safety.cameraevent.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedCameraEventEntryTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedCameraEventEntry("code-448", "name-852")
        assertEquals(CappedCameraEventEntryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newCappedCameraEventEntry("code-448", "name-852")
        try {
            item.moveTo(CappedCameraEventEntryStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedCameraEventEntryStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedCameraEventEntry("code-448", "name-852")
        item.status = CappedCameraEventEntryStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedCameraEventEntryStatus.REQUESTED) }
    }
}
