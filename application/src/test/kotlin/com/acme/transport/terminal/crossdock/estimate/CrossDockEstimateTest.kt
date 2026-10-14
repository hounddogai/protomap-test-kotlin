package com.acme.transport.terminal.crossdock.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockEstimateTest {
    @Test
    fun `starts in created`() {
        val item = newCrossDockEstimate("code-579", "title-523")
        assertEquals(CrossDockEstimateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCrossDockEstimate("code-579", "title-523")
        try {
            item.moveTo(CrossDockEstimateStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockEstimateStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCrossDockEstimate("code-579", "title-523")
        item.status = CrossDockEstimateStatus.RETIRED
        assertFails("move") { item.moveTo(CrossDockEstimateStatus.CREATED) }
    }
}
