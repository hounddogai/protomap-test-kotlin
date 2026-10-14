package com.acme.transport.customs.clearance.clearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualClearanceOverrideTest {
    @Test
    fun `starts in planned`() {
        val item = newManualClearanceOverride("code-566", "name-588")
        assertEquals(ManualClearanceOverrideStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newManualClearanceOverride("code-566", "name-588")
        try {
            item.moveTo(ManualClearanceOverrideStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualClearanceOverrideStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newManualClearanceOverride("code-566", "name-588")
        item.status = ManualClearanceOverrideStatus.CANCELLED
        assertFails("move") { item.moveTo(ManualClearanceOverrideStatus.PLANNED) }
    }
}
