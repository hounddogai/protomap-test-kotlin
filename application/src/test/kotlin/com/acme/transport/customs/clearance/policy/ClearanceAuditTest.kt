package com.acme.transport.customs.clearance.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceAuditTest {
    @Test
    fun `starts in proposed`() {
        val item = newClearanceAudit("code-370", "title-252")
        assertEquals(ClearanceAuditStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newClearanceAudit("code-370", "title-252")
        try {
            item.moveTo(ClearanceAuditStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceAuditStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newClearanceAudit("code-370", "title-252")
        item.status = ClearanceAuditStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ClearanceAuditStatus.PROPOSED) }
    }
}
