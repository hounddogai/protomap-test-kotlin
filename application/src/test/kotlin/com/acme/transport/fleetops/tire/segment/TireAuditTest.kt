package com.acme.transport.fleetops.tire.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireAuditTest {
    @Test
    fun `starts in proposed`() {
        val item = newTireAudit("code-744", "title-908")
        assertEquals(TireAuditStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newTireAudit("code-744", "title-908")
        try {
            item.moveTo(TireAuditStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireAuditStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTireAudit("code-744", "title-908")
        item.status = TireAuditStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TireAuditStatus.PROPOSED) }
    }
}
