package com.acme.transport.customs.hold.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualHoldEstimateTest {
    @Test
    fun `starts in open`() {
        val item = newManualHoldEstimate("code-96", "title-495")
        assertEquals(ManualHoldEstimateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newManualHoldEstimate("code-96", "title-495")
        try {
            item.moveTo(ManualHoldEstimateStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualHoldEstimateStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newManualHoldEstimate("code-96", "title-495")
        item.status = ManualHoldEstimateStatus.ARCHIVED
        assertFails("move") { item.moveTo(ManualHoldEstimateStatus.OPEN) }
    }
}
