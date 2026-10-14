package com.acme.transport.fleetops.utilization.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyUtilizationBaselineTest {
    @Test
    fun `starts in new`() {
        val item = newLegacyUtilizationBaseline("code-593", "title-187")
        assertEquals(LegacyUtilizationBaselineStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLegacyUtilizationBaseline("code-593", "title-187")
        try {
            item.moveTo(LegacyUtilizationBaselineStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyUtilizationBaselineStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacyUtilizationBaseline("code-593", "title-187")
        item.status = LegacyUtilizationBaselineStatus.FAILED
        assertFails("move") { item.moveTo(LegacyUtilizationBaselineStatus.NEW) }
    }
}
