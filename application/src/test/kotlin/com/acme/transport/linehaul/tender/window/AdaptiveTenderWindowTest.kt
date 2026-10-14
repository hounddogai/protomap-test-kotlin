package com.acme.transport.linehaul.tender.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveTenderWindowTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveTenderWindow("code-275", "title-567")
        assertEquals(AdaptiveTenderWindowStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newAdaptiveTenderWindow("code-275", "title-567")
        try {
            item.moveTo(AdaptiveTenderWindowStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveTenderWindowStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveTenderWindow("code-275", "title-567")
        item.status = AdaptiveTenderWindowStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveTenderWindowStatus.NEW) }
    }
}
