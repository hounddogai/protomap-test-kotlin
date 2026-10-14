package com.acme.transport.linehaul.backhaul.backhaulquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedBackhaulWindowTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedBackhaulWindow("code-100", "title-389")
        assertEquals(CappedBackhaulWindowStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newCappedBackhaulWindow("code-100", "title-389")
        try {
            item.moveTo(CappedBackhaulWindowStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedBackhaulWindowStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedBackhaulWindow("code-100", "title-389")
        item.status = CappedBackhaulWindowStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedBackhaulWindowStatus.REQUESTED) }
    }
}
