package com.acme.transport.safety.violation.manualviolationquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackViolationSlotTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackViolationSlot("code-228", "title-82")
        assertEquals(FallbackViolationSlotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newFallbackViolationSlot("code-228", "title-82")
        try {
            item.moveTo(FallbackViolationSlotStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackViolationSlotStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackViolationSlot("code-228", "title-82")
        item.status = FallbackViolationSlotStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackViolationSlotStatus.OPEN) }
    }
}
