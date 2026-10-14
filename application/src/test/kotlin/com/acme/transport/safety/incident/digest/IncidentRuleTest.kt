package com.acme.transport.safety.incident.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentRuleTest {
    @Test
    fun `starts in draft`() {
        val item = newIncidentRule("code-805", "label-513")
        assertEquals(IncidentRuleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newIncidentRule("code-805", "label-513")
        try {
            item.moveTo(IncidentRuleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentRuleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newIncidentRule("code-805", "label-513")
        item.status = IncidentRuleStatus.CLOSED
        assertFails("move") { item.moveTo(IncidentRuleStatus.DRAFT) }
    }
}
