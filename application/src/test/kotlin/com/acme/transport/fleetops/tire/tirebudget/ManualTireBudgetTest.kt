package com.acme.transport.fleetops.tire.tirebudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualTireBudgetTest {
    @Test
    fun `starts in idle`() {
        val item = newManualTireBudget("code-533", "name-413")
        assertEquals(ManualTireBudgetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newManualTireBudget("code-533", "name-413")
        try {
            item.moveTo(ManualTireBudgetStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualTireBudgetStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualTireBudget("code-533", "name-413")
        item.status = ManualTireBudgetStatus.STOPPED
        assertFails("move") { item.moveTo(ManualTireBudgetStatus.IDLE) }
    }
}
