package com.acme.transport.safety.violation.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyViolationVarianceTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyViolationVariance("code-746", "title-339")
        assertEquals(LegacyViolationVarianceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newLegacyViolationVariance("code-746", "title-339")
        try {
            item.moveTo(LegacyViolationVarianceStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyViolationVarianceStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyViolationVariance("code-746", "title-339")
        item.status = LegacyViolationVarianceStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyViolationVarianceStatus.IDLE) }
    }
}
