package com.acme.transport.linehaul.backhaul.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackBackhaulRequestTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackBackhaulRequest("code-380", "title-495")
        assertEquals(FallbackBackhaulRequestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newFallbackBackhaulRequest("code-380", "title-495")
        try {
            item.moveTo(FallbackBackhaulRequestStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackBackhaulRequestStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackBackhaulRequest("code-380", "title-495")
        item.status = FallbackBackhaulRequestStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackBackhaulRequestStatus.IDLE) }
    }
}
