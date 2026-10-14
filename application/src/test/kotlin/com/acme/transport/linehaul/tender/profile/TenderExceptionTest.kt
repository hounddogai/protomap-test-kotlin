package com.acme.transport.linehaul.tender.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderExceptionTest {
    @Test
    fun `starts in idle`() {
        val item = newTenderException("code-800", "name-948")
        assertEquals(TenderExceptionStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newTenderException("code-800", "name-948")
        try {
            item.moveTo(TenderExceptionStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderExceptionStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTenderException("code-800", "name-948")
        item.status = TenderExceptionStatus.STOPPED
        assertFails("move") { item.moveTo(TenderExceptionStatus.IDLE) }
    }
}
