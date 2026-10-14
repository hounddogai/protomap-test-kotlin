package com.acme.transport.safety.violation.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationRollupTest {
    @Test
    fun `starts in idle`() {
        val item = newViolationRollup("code-877", "label-40")
        assertEquals(ViolationRollupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newViolationRollup("code-877", "label-40")
        try {
            item.moveTo(ViolationRollupStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationRollupStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newViolationRollup("code-877", "label-40")
        item.status = ViolationRollupStatus.STOPPED
        assertFails("move") { item.moveTo(ViolationRollupStatus.IDLE) }
    }
}
