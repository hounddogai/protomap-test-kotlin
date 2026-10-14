package com.acme.transport.safety.violation.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingViolationWindowTest {
    @Test
    fun `starts in created`() {
        val item = newRollingViolationWindow("code-204", "name-82")
        assertEquals(RollingViolationWindowStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRollingViolationWindow("code-204", "name-82")
        try {
            item.moveTo(RollingViolationWindowStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingViolationWindowStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingViolationWindow("code-204", "name-82")
        item.status = RollingViolationWindowStatus.RETIRED
        assertFails("move") { item.moveTo(RollingViolationWindowStatus.CREATED) }
    }
}
