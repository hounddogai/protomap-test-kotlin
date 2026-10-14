package com.acme.transport.lastmile.drop.droprequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDropRequestTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientDropRequest("code-901", "title-874")
        assertEquals(LenientDropRequestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newLenientDropRequest("code-901", "title-874")
        try {
            item.moveTo(LenientDropRequestStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDropRequestStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientDropRequest("code-901", "title-874")
        item.status = LenientDropRequestStatus.CLOSED
        assertFails("move") { item.moveTo(LenientDropRequestStatus.DRAFT) }
    }
}
