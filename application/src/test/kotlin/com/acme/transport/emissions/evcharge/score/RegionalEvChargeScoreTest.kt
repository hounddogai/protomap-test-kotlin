package com.acme.transport.emissions.evcharge.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalEvChargeScoreTest {
    @Test
    fun `starts in open`() {
        val item = newRegionalEvChargeScore("code-267", "name-656")
        assertEquals(RegionalEvChargeScoreStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newRegionalEvChargeScore("code-267", "name-656")
        try {
            item.moveTo(RegionalEvChargeScoreStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalEvChargeScoreStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRegionalEvChargeScore("code-267", "name-656")
        item.status = RegionalEvChargeScoreStatus.ARCHIVED
        assertFails("move") { item.moveTo(RegionalEvChargeScoreStatus.OPEN) }
    }
}
