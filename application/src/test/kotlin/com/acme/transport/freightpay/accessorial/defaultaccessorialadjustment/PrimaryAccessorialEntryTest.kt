package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryAccessorialEntryTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryAccessorialEntry("code-540", "name-532")
        assertEquals(PrimaryAccessorialEntryStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newPrimaryAccessorialEntry("code-540", "name-532")
        try {
            item.moveTo(PrimaryAccessorialEntryStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryAccessorialEntryStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryAccessorialEntry("code-540", "name-532")
        item.status = PrimaryAccessorialEntryStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryAccessorialEntryStatus.NEW) }
    }
}
