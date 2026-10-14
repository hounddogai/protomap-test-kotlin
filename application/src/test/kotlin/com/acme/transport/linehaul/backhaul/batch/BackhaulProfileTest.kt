package com.acme.transport.linehaul.backhaul.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulProfileTest {
    @Test
    fun `starts in planned`() {
        val item = newBackhaulProfile("code-18", "label-974")
        assertEquals(BackhaulProfileStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newBackhaulProfile("code-18", "label-974")
        try {
            item.moveTo(BackhaulProfileStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulProfileStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBackhaulProfile("code-18", "label-974")
        item.status = BackhaulProfileStatus.CANCELLED
        assertFails("move") { item.moveTo(BackhaulProfileStatus.PLANNED) }
    }
}
