package com.acme.transport.customs.declaration.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationQuotaTest {
    @Test
    fun `starts in idle`() {
        val item = newDeclarationQuota("code-493", "label-687")
        assertEquals(DeclarationQuotaStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newDeclarationQuota("code-493", "label-687")
        try {
            item.moveTo(DeclarationQuotaStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationQuotaStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDeclarationQuota("code-493", "label-687")
        item.status = DeclarationQuotaStatus.STOPPED
        assertFails("move") { item.moveTo(DeclarationQuotaStatus.IDLE) }
    }
}
