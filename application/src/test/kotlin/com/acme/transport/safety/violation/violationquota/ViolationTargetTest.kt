package com.acme.transport.safety.violation.violationquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationTargetTest {
    @Test
    fun `starts in requested`() {
        val item = newViolationTarget("code-77", "label-181")
        assertEquals(ViolationTargetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newViolationTarget("code-77", "label-181")
        try {
            item.moveTo(ViolationTargetStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationTargetStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newViolationTarget("code-77", "label-181")
        item.status = ViolationTargetStatus.EXPIRED
        assertFails("move") { item.moveTo(ViolationTargetStatus.REQUESTED) }
    }
}
