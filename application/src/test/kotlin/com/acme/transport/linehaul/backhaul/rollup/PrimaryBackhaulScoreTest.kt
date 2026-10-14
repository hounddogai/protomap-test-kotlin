package com.acme.transport.linehaul.backhaul.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryBackhaulScoreTest {
    @Test
    fun `starts in idle`() {
        val item = newPrimaryBackhaulScore("code-926", "title-841")
        assertEquals(PrimaryBackhaulScoreStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newPrimaryBackhaulScore("code-926", "title-841")
        try {
            item.moveTo(PrimaryBackhaulScoreStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryBackhaulScoreStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPrimaryBackhaulScore("code-926", "title-841")
        item.status = PrimaryBackhaulScoreStatus.STOPPED
        assertFails("move") { item.moveTo(PrimaryBackhaulScoreStatus.IDLE) }
    }
}
