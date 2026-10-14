package com.acme.transport.fleetops.defect.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveDefectRecordTest {
    @Test
    fun `starts in proposed`() {
        val item = newAdaptiveDefectRecord("code-303", "name-511")
        assertEquals(AdaptiveDefectRecordStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newAdaptiveDefectRecord("code-303", "name-511")
        try {
            item.moveTo(AdaptiveDefectRecordStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveDefectRecordStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAdaptiveDefectRecord("code-303", "name-511")
        item.status = AdaptiveDefectRecordStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AdaptiveDefectRecordStatus.PROPOSED) }
    }
}
