package com.acme.transport.fleetops.defect.defectcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredDefectAuditTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredDefectAudit("code-517", "title-155")
        assertEquals(TieredDefectAuditStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newTieredDefectAudit("code-517", "title-155")
        try {
            item.moveTo(TieredDefectAuditStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredDefectAuditStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredDefectAudit("code-517", "title-155")
        item.status = TieredDefectAuditStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredDefectAuditStatus.PROPOSED) }
    }
}
