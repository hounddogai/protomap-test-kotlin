package com.acme.transport.lastmile.drop.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropQuotaTest {
    @Test
    fun `starts in draft`() {
        val item = newDropQuota("code-554", "title-60")
        assertEquals(DropQuotaStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newDropQuota("code-554", "title-60")
        try {
            item.moveTo(DropQuotaStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropQuotaStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDropQuota("code-554", "title-60")
        item.status = DropQuotaStatus.CLOSED
        assertFails("move") { item.moveTo(DropQuotaStatus.DRAFT) }
    }
}
