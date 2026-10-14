package com.acme.transport.customs.hold.strictholdaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictHoldAuditTest {
    @Test
    fun `starts in created`() {
        val item = newStrictHoldAudit("code-249", "label-640")
        assertEquals(StrictHoldAuditStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newStrictHoldAudit("code-249", "label-640")
        try {
            item.moveTo(StrictHoldAuditStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictHoldAuditStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newStrictHoldAudit("code-249", "label-640")
        item.status = StrictHoldAuditStatus.RETIRED
        assertFails("move") { item.moveTo(StrictHoldAuditStatus.CREATED) }
    }
}
