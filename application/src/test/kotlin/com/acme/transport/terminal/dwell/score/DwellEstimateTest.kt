package com.acme.transport.terminal.dwell.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellEstimateTest {
    @Test
    fun `starts in idle`() {
        val item = newDwellEstimate("code-266", "title-320")
        assertEquals(DwellEstimateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newDwellEstimate("code-266", "title-320")
        try {
            item.moveTo(DwellEstimateStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellEstimateStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDwellEstimate("code-266", "title-320")
        item.status = DwellEstimateStatus.STOPPED
        assertFails("move") { item.moveTo(DwellEstimateStatus.IDLE) }
    }
}
