package com.acme.transport.terminal.traileryard.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardEstimateTest {
    @Test
    fun `starts in idle`() {
        val item = newTrailerYardEstimate("code-862", "label-903")
        assertEquals(TrailerYardEstimateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newTrailerYardEstimate("code-862", "label-903")
        try {
            item.moveTo(TrailerYardEstimateStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardEstimateStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTrailerYardEstimate("code-862", "label-903")
        item.status = TrailerYardEstimateStatus.STOPPED
        assertFails("move") { item.moveTo(TrailerYardEstimateStatus.IDLE) }
    }
}
