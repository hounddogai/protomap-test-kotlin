package com.acme.transport.terminal.dwell.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellRecordTest {
    @Test
    fun `starts in planned`() {
        val item = newDwellRecord("code-51", "title-717")
        assertEquals(DwellRecordStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newDwellRecord("code-51", "title-717")
        try {
            item.moveTo(DwellRecordStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellRecordStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDwellRecord("code-51", "title-717")
        item.status = DwellRecordStatus.CANCELLED
        assertFails("move") { item.moveTo(DwellRecordStatus.PLANNED) }
    }
}
