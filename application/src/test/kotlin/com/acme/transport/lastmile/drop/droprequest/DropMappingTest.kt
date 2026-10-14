package com.acme.transport.lastmile.drop.droprequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropMappingTest {
    @Test
    fun `starts in requested`() {
        val item = newDropMapping("code-472", "label-595")
        assertEquals(DropMappingStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDropMapping("code-472", "label-595")
        try {
            item.moveTo(DropMappingStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropMappingStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDropMapping("code-472", "label-595")
        item.status = DropMappingStatus.EXPIRED
        assertFails("move") { item.moveTo(DropMappingStatus.REQUESTED) }
    }
}
