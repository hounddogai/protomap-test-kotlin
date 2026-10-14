package com.acme.transport.fleetops.defect.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDefectTargetTest {
    @Test
    fun `starts in created`() {
        val item = newManualDefectTarget("code-967", "label-69")
        assertEquals(ManualDefectTargetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newManualDefectTarget("code-967", "label-69")
        try {
            item.moveTo(ManualDefectTargetStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDefectTargetStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualDefectTarget("code-967", "label-69")
        item.status = ManualDefectTargetStatus.RETIRED
        assertFails("move") { item.moveTo(ManualDefectTargetStatus.CREATED) }
    }
}
