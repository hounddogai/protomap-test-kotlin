package com.acme.transport.fleetops.tire.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireBatchTest {
    @Test
    fun `starts in open`() {
        val item = newTireBatch("code-404", "title-255")
        assertEquals(TireBatchStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newTireBatch("code-404", "title-255")
        try {
            item.moveTo(TireBatchStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireBatchStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTireBatch("code-404", "title-255")
        item.status = TireBatchStatus.ARCHIVED
        assertFails("move") { item.moveTo(TireBatchStatus.OPEN) }
    }
}
