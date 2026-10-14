package com.acme.transport.customs.inspection.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackInspectionOverrideTest {
    @Test
    fun `starts in planned`() {
        val item = newFallbackInspectionOverride("code-672", "title-588")
        assertEquals(FallbackInspectionOverrideStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newFallbackInspectionOverride("code-672", "title-588")
        try {
            item.moveTo(FallbackInspectionOverrideStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackInspectionOverrideStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFallbackInspectionOverride("code-672", "title-588")
        item.status = FallbackInspectionOverrideStatus.CANCELLED
        assertFails("move") { item.moveTo(FallbackInspectionOverrideStatus.PLANNED) }
    }
}
