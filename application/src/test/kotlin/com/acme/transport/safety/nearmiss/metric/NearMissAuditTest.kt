package com.acme.transport.safety.nearmiss.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissAuditTest {
    @Test
    fun `starts in idle`() {
        val item = newNearMissAudit("code-940", "name-443")
        assertEquals(NearMissAuditStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newNearMissAudit("code-940", "name-443")
        try {
            item.moveTo(NearMissAuditStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissAuditStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newNearMissAudit("code-940", "name-443")
        item.status = NearMissAuditStatus.STOPPED
        assertFails("move") { item.moveTo(NearMissAuditStatus.IDLE) }
    }
}
