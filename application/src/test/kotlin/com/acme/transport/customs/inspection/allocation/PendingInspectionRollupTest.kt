package com.acme.transport.customs.inspection.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingInspectionRollupTest {
    @Test
    fun `starts in proposed`() {
        val item = newPendingInspectionRollup("code-527", "label-321")
        assertEquals(PendingInspectionRollupStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newPendingInspectionRollup("code-527", "label-321")
        try {
            item.moveTo(PendingInspectionRollupStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingInspectionRollupStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPendingInspectionRollup("code-527", "label-321")
        item.status = PendingInspectionRollupStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PendingInspectionRollupStatus.PROPOSED) }
    }
}
