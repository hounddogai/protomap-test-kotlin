package com.acme.transport.linehaul.backhaul.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictBackhaulAuditTest {
    @Test
    fun `starts in new`() {
        val item = newStrictBackhaulAudit("code-39", "title-191")
        assertEquals(StrictBackhaulAuditStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newStrictBackhaulAudit("code-39", "title-191")
        try {
            item.moveTo(StrictBackhaulAuditStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictBackhaulAuditStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newStrictBackhaulAudit("code-39", "title-191")
        item.status = StrictBackhaulAuditStatus.FAILED
        assertFails("move") { item.moveTo(StrictBackhaulAuditStatus.NEW) }
    }
}
