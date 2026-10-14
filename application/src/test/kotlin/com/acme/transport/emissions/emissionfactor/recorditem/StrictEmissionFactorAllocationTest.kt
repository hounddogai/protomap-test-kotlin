package com.acme.transport.emissions.emissionfactor.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictEmissionFactorAllocationTest {
    @Test
    fun `starts in idle`() {
        val item = newStrictEmissionFactorAllocation("code-706", "title-969")
        assertEquals(StrictEmissionFactorAllocationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newStrictEmissionFactorAllocation("code-706", "title-969")
        try {
            item.moveTo(StrictEmissionFactorAllocationStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictEmissionFactorAllocationStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newStrictEmissionFactorAllocation("code-706", "title-969")
        item.status = StrictEmissionFactorAllocationStatus.STOPPED
        assertFails("move") { item.moveTo(StrictEmissionFactorAllocationStatus.IDLE) }
    }
}
