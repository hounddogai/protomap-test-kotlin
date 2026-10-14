package com.acme.transport.emissions.evcharge.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualEvChargeAuditTest {
    @Test
    fun `starts in open`() {
        val item = newManualEvChargeAudit("code-491", "title-886")
        assertEquals(ManualEvChargeAuditStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newManualEvChargeAudit("code-491", "title-886")
        try {
            item.moveTo(ManualEvChargeAuditStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualEvChargeAuditStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newManualEvChargeAudit("code-491", "title-886")
        item.status = ManualEvChargeAuditStatus.ARCHIVED
        assertFails("move") { item.moveTo(ManualEvChargeAuditStatus.OPEN) }
    }
}
