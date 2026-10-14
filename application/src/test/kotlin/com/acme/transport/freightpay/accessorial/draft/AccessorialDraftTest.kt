package com.acme.transport.freightpay.accessorial.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialDraftTest {
    @Test
    fun `starts in new`() {
        val item = newAccessorialDraft("code-878", "name-769")
        assertEquals(AccessorialDraftStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newAccessorialDraft("code-878", "name-769")
        try {
            item.moveTo(AccessorialDraftStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialDraftStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAccessorialDraft("code-878", "name-769")
        item.status = AccessorialDraftStatus.FAILED
        assertFails("move") { item.moveTo(AccessorialDraftStatus.NEW) }
    }
}
