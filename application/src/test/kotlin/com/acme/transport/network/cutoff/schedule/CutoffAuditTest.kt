package com.acme.transport.network.cutoff.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffAuditTest {
    @Test
    fun `starts in new`() {
        val item = newCutoffAudit("code-725", "title-211")
        assertEquals(CutoffAuditStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newCutoffAudit("code-725", "title-211")
        try {
            item.moveTo(CutoffAuditStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffAuditStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCutoffAudit("code-725", "title-211")
        item.status = CutoffAuditStatus.FAILED
        assertFails("move") { item.moveTo(CutoffAuditStatus.NEW) }
    }
}
