package com.acme.transport.safety.violation.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationEstimateTest {
    @Test
    fun `starts in open`() {
        val item = newViolationEstimate("code-213", "name-879")
        assertEquals(ViolationEstimateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newViolationEstimate("code-213", "name-879")
        try {
            item.moveTo(ViolationEstimateStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationEstimateStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newViolationEstimate("code-213", "name-879")
        item.status = ViolationEstimateStatus.ARCHIVED
        assertFails("move") { item.moveTo(ViolationEstimateStatus.OPEN) }
    }
}
