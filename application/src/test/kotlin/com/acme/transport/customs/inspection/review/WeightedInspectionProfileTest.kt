package com.acme.transport.customs.inspection.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedInspectionProfileTest {
    @Test
    fun `starts in draft`() {
        val item = newWeightedInspectionProfile("code-58", "name-558")
        assertEquals(WeightedInspectionProfileStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newWeightedInspectionProfile("code-58", "name-558")
        try {
            item.moveTo(WeightedInspectionProfileStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedInspectionProfileStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newWeightedInspectionProfile("code-58", "name-558")
        item.status = WeightedInspectionProfileStatus.CLOSED
        assertFails("move") { item.moveTo(WeightedInspectionProfileStatus.DRAFT) }
    }
}
