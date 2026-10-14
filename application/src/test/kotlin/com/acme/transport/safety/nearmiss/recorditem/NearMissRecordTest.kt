package com.acme.transport.safety.nearmiss.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissRecordTest {
    @Test
    fun `starts in new`() {
        val item = newNearMissRecord("code-553", "title-908")
        assertEquals(NearMissRecordStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newNearMissRecord("code-553", "title-908")
        try {
            item.moveTo(NearMissRecordStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissRecordStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newNearMissRecord("code-553", "title-908")
        item.status = NearMissRecordStatus.FAILED
        assertFails("move") { item.moveTo(NearMissRecordStatus.NEW) }
    }
}
