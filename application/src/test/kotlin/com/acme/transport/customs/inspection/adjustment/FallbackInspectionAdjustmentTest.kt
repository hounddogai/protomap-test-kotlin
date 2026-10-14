package com.acme.transport.customs.inspection.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackInspectionAdjustmentTest {
    @Test
    fun `starts in created`() {
        val item = newFallbackInspectionAdjustment("code-293", "name-907")
        assertEquals(FallbackInspectionAdjustmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newFallbackInspectionAdjustment("code-293", "name-907")
        try {
            item.moveTo(FallbackInspectionAdjustmentStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackInspectionAdjustmentStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFallbackInspectionAdjustment("code-293", "name-907")
        item.status = FallbackInspectionAdjustmentStatus.RETIRED
        assertFails("move") { item.moveTo(FallbackInspectionAdjustmentStatus.CREATED) }
    }
}
