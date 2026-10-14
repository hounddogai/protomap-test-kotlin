package com.acme.transport.linehaul.backhaul.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulTargetTest {
    @Test
    fun `starts in idle`() {
        val item = newBackhaulTarget("code-563", "label-120")
        assertEquals(BackhaulTargetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newBackhaulTarget("code-563", "label-120")
        try {
            item.moveTo(BackhaulTargetStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulTargetStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBackhaulTarget("code-563", "label-120")
        item.status = BackhaulTargetStatus.STOPPED
        assertFails("move") { item.moveTo(BackhaulTargetStatus.IDLE) }
    }
}
