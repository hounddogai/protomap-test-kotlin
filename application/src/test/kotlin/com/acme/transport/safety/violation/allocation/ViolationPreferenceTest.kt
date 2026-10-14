package com.acme.transport.safety.violation.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationPreferenceTest {
    @Test
    fun `starts in idle`() {
        val item = newViolationPreference("code-166", "title-661")
        assertEquals(ViolationPreferenceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newViolationPreference("code-166", "title-661")
        try {
            item.moveTo(ViolationPreferenceStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationPreferenceStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newViolationPreference("code-166", "title-661")
        item.status = ViolationPreferenceStatus.STOPPED
        assertFails("move") { item.moveTo(ViolationPreferenceStatus.IDLE) }
    }
}
