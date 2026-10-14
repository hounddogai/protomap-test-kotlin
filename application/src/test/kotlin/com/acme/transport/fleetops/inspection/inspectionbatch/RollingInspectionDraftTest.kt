package com.acme.transport.fleetops.inspection.inspectionbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingInspectionDraftTest {
    @Test
    fun `starts in created`() {
        val item = newRollingInspectionDraft("code-912", "title-550")
        assertEquals(RollingInspectionDraftStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRollingInspectionDraft("code-912", "title-550")
        try {
            item.moveTo(RollingInspectionDraftStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingInspectionDraftStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingInspectionDraft("code-912", "title-550")
        item.status = RollingInspectionDraftStatus.RETIRED
        assertFails("move") { item.moveTo(RollingInspectionDraftStatus.CREATED) }
    }
}
