package com.acme.transport.terminal.dwell.dwellreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellThresholdTest {
    @Test
    fun `starts in new`() {
        val item = newDwellThreshold("code-641", "label-555")
        assertEquals(DwellThresholdStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newDwellThreshold("code-641", "label-555")
        try {
            item.moveTo(DwellThresholdStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellThresholdStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDwellThreshold("code-641", "label-555")
        item.status = DwellThresholdStatus.FAILED
        assertFails("move") { item.moveTo(DwellThresholdStatus.NEW) }
    }
}
