package com.acme.transport.safety.coaching.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryCoachingRecordTest {
    @Test
    fun `starts in idle`() {
        val item = newPrimaryCoachingRecord("code-677", "title-460")
        assertEquals(PrimaryCoachingRecordStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newPrimaryCoachingRecord("code-677", "title-460")
        try {
            item.moveTo(PrimaryCoachingRecordStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryCoachingRecordStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPrimaryCoachingRecord("code-677", "title-460")
        item.status = PrimaryCoachingRecordStatus.STOPPED
        assertFails("move") { item.moveTo(PrimaryCoachingRecordStatus.IDLE) }
    }
}
