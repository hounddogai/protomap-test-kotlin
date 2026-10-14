package com.acme.transport.terminal.dwell.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedDwellBaselineTest {
    @Test
    fun `starts in idle`() {
        val item = newCappedDwellBaseline("code-598", "label-796")
        assertEquals(CappedDwellBaselineStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newCappedDwellBaseline("code-598", "label-796")
        try {
            item.moveTo(CappedDwellBaselineStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedDwellBaselineStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCappedDwellBaseline("code-598", "label-796")
        item.status = CappedDwellBaselineStatus.STOPPED
        assertFails("move") { item.moveTo(CappedDwellBaselineStatus.IDLE) }
    }
}
