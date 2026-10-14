package com.acme.transport.network.cutoff.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffScheduleTest {
    @Test
    fun `starts in new`() {
        val item = newCutoffSchedule("code-605", "title-565")
        assertEquals(CutoffScheduleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCutoffSchedule("code-605", "title-565")
        try {
            item.moveTo(CutoffScheduleStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffScheduleStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCutoffSchedule("code-605", "title-565")
        item.status = CutoffScheduleStatus.FAILED
        assertFails("move") { item.moveTo(CutoffScheduleStatus.NEW) }
    }
}
