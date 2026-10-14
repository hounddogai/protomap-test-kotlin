package com.acme.transport.freightpay.accessorial.accessorialentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedAccessorialEntryTest {
    @Test
    fun `starts in new`() {
        val item = newCappedAccessorialEntry("code-746", "name-804")
        assertEquals(CappedAccessorialEntryStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newCappedAccessorialEntry("code-746", "name-804")
        try {
            item.moveTo(CappedAccessorialEntryStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedAccessorialEntryStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCappedAccessorialEntry("code-746", "name-804")
        item.status = CappedAccessorialEntryStatus.FAILED
        assertFails("move") { item.moveTo(CappedAccessorialEntryStatus.NEW) }
    }
}
