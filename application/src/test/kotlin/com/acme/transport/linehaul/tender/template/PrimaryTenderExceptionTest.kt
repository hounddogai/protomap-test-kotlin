package com.acme.transport.linehaul.tender.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryTenderExceptionTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryTenderException("code-914", "title-853")
        assertEquals(PrimaryTenderExceptionStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPrimaryTenderException("code-914", "title-853")
        try {
            item.moveTo(PrimaryTenderExceptionStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryTenderExceptionStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryTenderException("code-914", "title-853")
        item.status = PrimaryTenderExceptionStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryTenderExceptionStatus.REQUESTED) }
    }
}
