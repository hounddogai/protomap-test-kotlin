package com.acme.transport.freightpay.audit.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditMappingTest {
    @Test
    fun `starts in proposed`() {
        val item = newAuditMapping("code-753", "name-569")
        assertEquals(AuditMappingStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newAuditMapping("code-753", "name-569")
        try {
            item.moveTo(AuditMappingStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditMappingStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAuditMapping("code-753", "name-569")
        item.status = AuditMappingStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AuditMappingStatus.PROPOSED) }
    }
}
