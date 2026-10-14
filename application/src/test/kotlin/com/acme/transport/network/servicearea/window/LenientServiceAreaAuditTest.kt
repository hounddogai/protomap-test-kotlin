package com.acme.transport.network.servicearea.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientServiceAreaAuditTest {
    @Test
    fun `starts in requested`() {
        val item = newLenientServiceAreaAudit("code-924", "title-640")
        assertEquals(LenientServiceAreaAuditStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLenientServiceAreaAudit("code-924", "title-640")
        try {
            item.moveTo(LenientServiceAreaAuditStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientServiceAreaAuditStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLenientServiceAreaAudit("code-924", "title-640")
        item.status = LenientServiceAreaAuditStatus.EXPIRED
        assertFails("move") { item.moveTo(LenientServiceAreaAuditStatus.REQUESTED) }
    }
}
