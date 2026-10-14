package com.acme.transport.fleetops.utilization.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalUtilizationPolicyTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalUtilizationPolicy("code-918", "name-372")
        assertEquals(SeasonalUtilizationPolicyStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newSeasonalUtilizationPolicy("code-918", "name-372")
        try {
            item.moveTo(SeasonalUtilizationPolicyStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalUtilizationPolicyStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalUtilizationPolicy("code-918", "name-372")
        item.status = SeasonalUtilizationPolicyStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalUtilizationPolicyStatus.OPEN) }
    }
}
