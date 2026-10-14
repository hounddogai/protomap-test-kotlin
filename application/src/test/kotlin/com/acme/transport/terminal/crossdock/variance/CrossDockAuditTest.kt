package com.acme.transport.terminal.crossdock.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockAuditTest {
    @Test
    fun `starts in created`() {
        val item = newCrossDockAudit("code-262", "name-689")
        assertEquals(CrossDockAuditStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCrossDockAudit("code-262", "name-689")
        try {
            item.moveTo(CrossDockAuditStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockAuditStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCrossDockAudit("code-262", "name-689")
        item.status = CrossDockAuditStatus.RETIRED
        assertFails("move") { item.moveTo(CrossDockAuditStatus.CREATED) }
    }
}
