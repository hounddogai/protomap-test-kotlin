package com.acme.transport.safety.incident.incidentmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentBudgetTest {
    @Test
    fun `starts in new`() {
        val item = newIncidentBudget("code-961", "name-849")
        assertEquals(IncidentBudgetStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newIncidentBudget("code-961", "name-849")
        try {
            item.moveTo(IncidentBudgetStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentBudgetStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newIncidentBudget("code-961", "name-849")
        item.status = IncidentBudgetStatus.FAILED
        assertFails("move") { item.moveTo(IncidentBudgetStatus.NEW) }
    }
}
