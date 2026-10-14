package com.acme.transport.safety.nearmiss.missestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedNearMissRecordTest {
    @Test
    fun `starts in new`() {
        val item = newCappedNearMissRecord("code-443", "title-144")
        assertEquals(CappedNearMissRecordStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCappedNearMissRecord("code-443", "title-144")
        try {
            item.moveTo(CappedNearMissRecordStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedNearMissRecordStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCappedNearMissRecord("code-443", "title-144")
        item.status = CappedNearMissRecordStatus.FAILED
        assertFails("move") { item.moveTo(CappedNearMissRecordStatus.NEW) }
    }
}
