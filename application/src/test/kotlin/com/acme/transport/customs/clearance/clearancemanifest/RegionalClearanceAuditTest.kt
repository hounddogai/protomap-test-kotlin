package com.acme.transport.customs.clearance.clearancemanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalClearanceAuditTest {
    @Test
    fun `starts in planned`() {
        val item = newRegionalClearanceAudit("code-625", "name-329")
        assertEquals(RegionalClearanceAuditStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRegionalClearanceAudit("code-625", "name-329")
        try {
            item.moveTo(RegionalClearanceAuditStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalClearanceAuditStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRegionalClearanceAudit("code-625", "name-329")
        item.status = RegionalClearanceAuditStatus.CANCELLED
        assertFails("move") { item.moveTo(RegionalClearanceAuditStatus.PLANNED) }
    }
}
