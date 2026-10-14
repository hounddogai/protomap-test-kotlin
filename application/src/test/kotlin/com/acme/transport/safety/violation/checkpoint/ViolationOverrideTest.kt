package com.acme.transport.safety.violation.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationOverrideTest {
    @Test
    fun `starts in new`() {
        val item = newViolationOverride("code-858", "name-81")
        assertEquals(ViolationOverrideStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newViolationOverride("code-858", "name-81")
        try {
            item.moveTo(ViolationOverrideStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationOverrideStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newViolationOverride("code-858", "name-81")
        item.status = ViolationOverrideStatus.FAILED
        assertFails("move") { item.moveTo(ViolationOverrideStatus.NEW) }
    }
}
