package com.acme.transport.linehaul.backhaul.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultBackhaulWindowTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultBackhaulWindow("code-795", "label-540")
        assertEquals(DefaultBackhaulWindowStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDefaultBackhaulWindow("code-795", "label-540")
        try {
            item.moveTo(DefaultBackhaulWindowStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultBackhaulWindowStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultBackhaulWindow("code-795", "label-540")
        item.status = DefaultBackhaulWindowStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultBackhaulWindowStatus.REQUESTED) }
    }
}
