package com.acme.transport.safety.violation.primaryviolationquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultViolationRollupTest {
    @Test
    fun `starts in open`() {
        val item = newDefaultViolationRollup("code-609", "label-125")
        assertEquals(DefaultViolationRollupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newDefaultViolationRollup("code-609", "label-125")
        try {
            item.moveTo(DefaultViolationRollupStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultViolationRollupStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDefaultViolationRollup("code-609", "label-125")
        item.status = DefaultViolationRollupStatus.ARCHIVED
        assertFails("move") { item.moveTo(DefaultViolationRollupStatus.OPEN) }
    }
}
