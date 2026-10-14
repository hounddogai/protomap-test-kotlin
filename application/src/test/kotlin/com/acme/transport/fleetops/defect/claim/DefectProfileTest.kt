package com.acme.transport.fleetops.defect.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectProfileTest {
    @Test
    fun `starts in created`() {
        val item = newDefectProfile("code-185", "label-194")
        assertEquals(DefectProfileStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newDefectProfile("code-185", "label-194")
        try {
            item.moveTo(DefectProfileStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectProfileStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefectProfile("code-185", "label-194")
        item.status = DefectProfileStatus.RETIRED
        assertFails("move") { item.moveTo(DefectProfileStatus.CREATED) }
    }
}
