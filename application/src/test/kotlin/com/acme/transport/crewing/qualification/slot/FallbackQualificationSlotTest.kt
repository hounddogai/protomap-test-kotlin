package com.acme.transport.crewing.qualification.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackQualificationSlotTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackQualificationSlot("code-829", "title-135")
        assertEquals(FallbackQualificationSlotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newFallbackQualificationSlot("code-829", "title-135")
        try {
            item.moveTo(FallbackQualificationSlotStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackQualificationSlotStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackQualificationSlot("code-829", "title-135")
        item.status = FallbackQualificationSlotStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackQualificationSlotStatus.IDLE) }
    }
}
