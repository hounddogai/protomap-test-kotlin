package com.acme.transport.freightpay.accessorial.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialEntryTest {
    @Test
    fun `starts in open`() {
        val item = newAccessorialEntry("code-243", "title-958")
        assertEquals(AccessorialEntryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newAccessorialEntry("code-243", "title-958")
        try {
            item.moveTo(AccessorialEntryStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialEntryStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAccessorialEntry("code-243", "title-958")
        item.status = AccessorialEntryStatus.ARCHIVED
        assertFails("move") { item.moveTo(AccessorialEntryStatus.OPEN) }
    }
}
