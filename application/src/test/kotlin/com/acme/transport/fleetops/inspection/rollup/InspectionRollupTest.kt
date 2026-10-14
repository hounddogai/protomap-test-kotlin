package com.acme.transport.fleetops.inspection.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionRollupTest {
    @Test
    fun `starts in open`() {
        val item = newInspectionRollup("code-152", "label-585")
        assertEquals(InspectionRollupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newInspectionRollup("code-152", "label-585")
        try {
            item.moveTo(InspectionRollupStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionRollupStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newInspectionRollup("code-152", "label-585")
        item.status = InspectionRollupStatus.ARCHIVED
        assertFails("move") { item.moveTo(InspectionRollupStatus.OPEN) }
    }
}
