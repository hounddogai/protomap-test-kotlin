package com.acme.transport.crewing.restbreak.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakGroupTest {
    @Test
    fun `starts in requested`() {
        val item = newRestBreakGroup("code-912", "label-695")
        assertEquals(RestBreakGroupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRestBreakGroup("code-912", "label-695")
        try {
            item.moveTo(RestBreakGroupStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakGroupStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRestBreakGroup("code-912", "label-695")
        item.status = RestBreakGroupStatus.EXPIRED
        assertFails("move") { item.moveTo(RestBreakGroupStatus.REQUESTED) }
    }
}
