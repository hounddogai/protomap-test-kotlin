package com.acme.transport.freightpay.accessorial.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultAccessorialRollupTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultAccessorialRollup("code-281", "name-983")
        assertEquals(DefaultAccessorialRollupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDefaultAccessorialRollup("code-281", "name-983")
        try {
            item.moveTo(DefaultAccessorialRollupStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultAccessorialRollupStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultAccessorialRollup("code-281", "name-983")
        item.status = DefaultAccessorialRollupStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultAccessorialRollupStatus.REQUESTED) }
    }
}
