package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationEntryTest {
    @Test
    fun `starts in planned`() {
        val item = newUtilizationEntry("code-730", "title-875")
        assertEquals(UtilizationEntryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newUtilizationEntry("code-730", "title-875")
        try {
            item.moveTo(UtilizationEntryStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationEntryStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newUtilizationEntry("code-730", "title-875")
        item.status = UtilizationEntryStatus.CANCELLED
        assertFails("move") { item.moveTo(UtilizationEntryStatus.PLANNED) }
    }
}
