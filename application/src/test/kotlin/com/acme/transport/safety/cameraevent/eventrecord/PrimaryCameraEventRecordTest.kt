package com.acme.transport.safety.cameraevent.eventrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryCameraEventRecordTest {
    @Test
    fun `starts in proposed`() {
        val item = newPrimaryCameraEventRecord("code-296", "title-843")
        assertEquals(PrimaryCameraEventRecordStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newPrimaryCameraEventRecord("code-296", "title-843")
        try {
            item.moveTo(PrimaryCameraEventRecordStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryCameraEventRecordStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPrimaryCameraEventRecord("code-296", "title-843")
        item.status = PrimaryCameraEventRecordStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PrimaryCameraEventRecordStatus.PROPOSED) }
    }
}
