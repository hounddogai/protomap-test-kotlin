package com.acme.transport.freightpay.audit.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualAuditOverrideTest {
    @Test
    fun `starts in draft`() {
        val item = newManualAuditOverride("code-647", "name-137")
        assertEquals(ManualAuditOverrideStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newManualAuditOverride("code-647", "name-137")
        try {
            item.moveTo(ManualAuditOverrideStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualAuditOverrideStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualAuditOverride("code-647", "name-137")
        item.status = ManualAuditOverrideStatus.CLOSED
        assertFails("move") { item.moveTo(ManualAuditOverrideStatus.DRAFT) }
    }
}
