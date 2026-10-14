package com.acme.transport.terminal.dwell.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDwellEstimateTest {
    @Test
    fun `starts in draft`() {
        val item = newManualDwellEstimate("code-555", "name-994")
        assertEquals(ManualDwellEstimateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newManualDwellEstimate("code-555", "name-994")
        try {
            item.moveTo(ManualDwellEstimateStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDwellEstimateStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualDwellEstimate("code-555", "name-994")
        item.status = ManualDwellEstimateStatus.CLOSED
        assertFails("move") { item.moveTo(ManualDwellEstimateStatus.DRAFT) }
    }
}
