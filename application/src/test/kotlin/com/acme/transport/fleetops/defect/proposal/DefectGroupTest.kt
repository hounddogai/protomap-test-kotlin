package com.acme.transport.fleetops.defect.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectGroupTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefectGroup("code-17", "title-668")
        assertEquals(DefectGroupStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newDefectGroup("code-17", "title-668")
        try {
            item.moveTo(DefectGroupStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectGroupStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefectGroup("code-17", "title-668")
        item.status = DefectGroupStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefectGroupStatus.PROPOSED) }
    }
}
