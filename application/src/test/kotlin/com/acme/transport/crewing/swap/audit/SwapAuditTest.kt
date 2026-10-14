package com.acme.transport.crewing.swap.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapAuditTest {
    @Test
    fun `starts in proposed`() {
        val item = newSwapAudit("code-476", "label-874")
        assertEquals(SwapAuditStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newSwapAudit("code-476", "label-874")
        try {
            item.moveTo(SwapAuditStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapAuditStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newSwapAudit("code-476", "label-874")
        item.status = SwapAuditStatus.WITHDRAWN
        assertFails("move") { item.moveTo(SwapAuditStatus.PROPOSED) }
    }
}
