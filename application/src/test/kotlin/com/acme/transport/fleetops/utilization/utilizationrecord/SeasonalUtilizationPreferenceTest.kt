package com.acme.transport.fleetops.utilization.utilizationrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalUtilizationPreferenceTest {
    @Test
    fun `starts in idle`() {
        val item = newSeasonalUtilizationPreference("code-660", "name-468")
        assertEquals(SeasonalUtilizationPreferenceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newSeasonalUtilizationPreference("code-660", "name-468")
        try {
            item.moveTo(SeasonalUtilizationPreferenceStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalUtilizationPreferenceStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSeasonalUtilizationPreference("code-660", "name-468")
        item.status = SeasonalUtilizationPreferenceStatus.STOPPED
        assertFails("move") { item.moveTo(SeasonalUtilizationPreferenceStatus.IDLE) }
    }
}
