package com.acme.transport.safety.violation.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationAdjustmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newViolationAdjustment("code-528", "name-442")
        assertEquals(ViolationAdjustmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newViolationAdjustment("code-528", "name-442")
        try {
            item.moveTo(ViolationAdjustmentStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationAdjustmentStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newViolationAdjustment("code-528", "name-442")
        item.status = ViolationAdjustmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ViolationAdjustmentStatus.PROPOSED) }
    }
}
