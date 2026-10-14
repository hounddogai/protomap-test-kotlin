package com.acme.transport.fleetops.tire.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictTireAuditTest {
    @Test
    fun `starts in requested`() {
        val item = newStrictTireAudit("code-265", "label-98")
        assertEquals(StrictTireAuditStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newStrictTireAudit("code-265", "label-98")
        try {
            item.moveTo(StrictTireAuditStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictTireAuditStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newStrictTireAudit("code-265", "label-98")
        item.status = StrictTireAuditStatus.EXPIRED
        assertFails("move") { item.moveTo(StrictTireAuditStatus.REQUESTED) }
    }
}
