package com.acme.transport.lastmile.parcel.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelAuditTest {
    @Test
    fun `starts in open`() {
        val item = newParcelAudit("code-20", "title-86")
        assertEquals(ParcelAuditStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newParcelAudit("code-20", "title-86")
        try {
            item.moveTo(ParcelAuditStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelAuditStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newParcelAudit("code-20", "title-86")
        item.status = ParcelAuditStatus.ARCHIVED
        assertFails("move") { item.moveTo(ParcelAuditStatus.OPEN) }
    }
}
