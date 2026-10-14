package com.acme.transport.safety.violation.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientViolationCycleTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientViolationCycle("code-321", "label-977")
        assertEquals(LenientViolationCycleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newLenientViolationCycle("code-321", "label-977")
        try {
            item.moveTo(LenientViolationCycleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientViolationCycleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientViolationCycle("code-321", "label-977")
        item.status = LenientViolationCycleStatus.CLOSED
        assertFails("move") { item.moveTo(LenientViolationCycleStatus.DRAFT) }
    }
}
