package com.acme.transport.customs.hold.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldMappingTest {
    @Test
    fun `starts in requested`() {
        val item = newHoldMapping("code-936", "name-323")
        assertEquals(HoldMappingStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newHoldMapping("code-936", "name-323")
        try {
            item.moveTo(HoldMappingStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldMappingStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newHoldMapping("code-936", "name-323")
        item.status = HoldMappingStatus.EXPIRED
        assertFails("move") { item.moveTo(HoldMappingStatus.REQUESTED) }
    }
}
