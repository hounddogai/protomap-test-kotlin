package com.acme.transport.safety.violation.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingViolationGroupTest {
    @Test
    fun `starts in open`() {
        val item = newRollingViolationGroup("code-814", "label-475")
        assertEquals(RollingViolationGroupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newRollingViolationGroup("code-814", "label-475")
        try {
            item.moveTo(RollingViolationGroupStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingViolationGroupStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRollingViolationGroup("code-814", "label-475")
        item.status = RollingViolationGroupStatus.ARCHIVED
        assertFails("move") { item.moveTo(RollingViolationGroupStatus.OPEN) }
    }
}
