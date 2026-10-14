package com.acme.transport.fleetops.utilization.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationRuleTest {
    @Test
    fun `starts in open`() {
        val item = newUtilizationRule("code-718", "name-771")
        assertEquals(UtilizationRuleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newUtilizationRule("code-718", "name-771")
        try {
            item.moveTo(UtilizationRuleStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationRuleStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newUtilizationRule("code-718", "name-771")
        item.status = UtilizationRuleStatus.ARCHIVED
        assertFails("move") { item.moveTo(UtilizationRuleStatus.OPEN) }
    }
}
