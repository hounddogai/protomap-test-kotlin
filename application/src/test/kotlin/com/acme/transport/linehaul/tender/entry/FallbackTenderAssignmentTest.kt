package com.acme.transport.linehaul.tender.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackTenderAssignmentTest {
    @Test
    fun `starts in created`() {
        val item = newFallbackTenderAssignment("code-741", "label-292")
        assertEquals(FallbackTenderAssignmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newFallbackTenderAssignment("code-741", "label-292")
        try {
            item.moveTo(FallbackTenderAssignmentStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackTenderAssignmentStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFallbackTenderAssignment("code-741", "label-292")
        item.status = FallbackTenderAssignmentStatus.RETIRED
        assertFails("move") { item.moveTo(FallbackTenderAssignmentStatus.CREATED) }
    }
}
