package com.acme.transport.safety.cameraevent.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryCameraEventTargetTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryCameraEventTarget("code-11", "title-96")
        assertEquals(PrimaryCameraEventTargetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newPrimaryCameraEventTarget("code-11", "title-96")
        try {
            item.moveTo(PrimaryCameraEventTargetStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryCameraEventTargetStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryCameraEventTarget("code-11", "title-96")
        item.status = PrimaryCameraEventTargetStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryCameraEventTargetStatus.OPEN) }
    }
}
