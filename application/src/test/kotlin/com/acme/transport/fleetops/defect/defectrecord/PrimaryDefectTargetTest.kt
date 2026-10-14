package com.acme.transport.fleetops.defect.defectrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryDefectTargetTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryDefectTarget("code-967", "label-85")
        assertEquals(PrimaryDefectTargetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPrimaryDefectTarget("code-967", "label-85")
        try {
            item.moveTo(PrimaryDefectTargetStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryDefectTargetStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryDefectTarget("code-967", "label-85")
        item.status = PrimaryDefectTargetStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryDefectTargetStatus.REQUESTED) }
    }
}
