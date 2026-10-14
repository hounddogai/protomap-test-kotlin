package com.acme.transport.emissions.idling.regionalidlingmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingSummaryTest {
    @Test
    fun `starts in requested`() {
        val item = newIdlingSummary("code-873", "title-105")
        assertEquals(IdlingSummaryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newIdlingSummary("code-873", "title-105")
        try {
            item.moveTo(IdlingSummaryStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingSummaryStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newIdlingSummary("code-873", "title-105")
        item.status = IdlingSummaryStatus.EXPIRED
        assertFails("move") { item.moveTo(IdlingSummaryStatus.REQUESTED) }
    }
}
