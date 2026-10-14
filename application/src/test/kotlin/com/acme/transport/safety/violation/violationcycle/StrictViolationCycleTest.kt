package com.acme.transport.safety.violation.violationcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictViolationCycleTest {
    @Test
    fun `starts in proposed`() {
        val item = newStrictViolationCycle("code-298", "name-730")
        assertEquals(StrictViolationCycleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newStrictViolationCycle("code-298", "name-730")
        try {
            item.moveTo(StrictViolationCycleStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictViolationCycleStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newStrictViolationCycle("code-298", "name-730")
        item.status = StrictViolationCycleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(StrictViolationCycleStatus.PROPOSED) }
    }
}
