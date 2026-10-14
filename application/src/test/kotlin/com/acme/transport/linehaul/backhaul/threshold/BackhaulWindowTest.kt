package com.acme.transport.linehaul.backhaul.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulWindowTest {
    @Test
    fun `starts in proposed`() {
        val item = newBackhaulWindow("code-772", "title-367")
        assertEquals(BackhaulWindowStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newBackhaulWindow("code-772", "title-367")
        try {
            item.moveTo(BackhaulWindowStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulWindowStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBackhaulWindow("code-772", "title-367")
        item.status = BackhaulWindowStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BackhaulWindowStatus.PROPOSED) }
    }
}
