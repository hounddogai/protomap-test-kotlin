package com.acme.transport.fleetops.utilization.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationOverrideTest {
    @Test
    fun `starts in planned`() {
        val item = newUtilizationOverride("code-78", "title-875")
        assertEquals(UtilizationOverrideStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newUtilizationOverride("code-78", "title-875")
        try {
            item.moveTo(UtilizationOverrideStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationOverrideStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newUtilizationOverride("code-78", "title-875")
        item.status = UtilizationOverrideStatus.CANCELLED
        assertFails("move") { item.moveTo(UtilizationOverrideStatus.PLANNED) }
    }
}
