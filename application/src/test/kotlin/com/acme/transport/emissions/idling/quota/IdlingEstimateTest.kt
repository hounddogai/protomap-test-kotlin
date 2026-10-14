package com.acme.transport.emissions.idling.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingEstimateTest {
    @Test
    fun `starts in open`() {
        val item = newIdlingEstimate("code-672", "name-656")
        assertEquals(IdlingEstimateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newIdlingEstimate("code-672", "name-656")
        try {
            item.moveTo(IdlingEstimateStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingEstimateStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newIdlingEstimate("code-672", "name-656")
        item.status = IdlingEstimateStatus.ARCHIVED
        assertFails("move") { item.moveTo(IdlingEstimateStatus.OPEN) }
    }
}
