package com.acme.transport.terminal.dwell.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellRollupTest {
    @Test
    fun `starts in draft`() {
        val item = newDwellRollup("code-180", "title-301")
        assertEquals(DwellRollupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newDwellRollup("code-180", "title-301")
        try {
            item.moveTo(DwellRollupStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellRollupStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDwellRollup("code-180", "title-301")
        item.status = DwellRollupStatus.CLOSED
        assertFails("move") { item.moveTo(DwellRollupStatus.DRAFT) }
    }
}
