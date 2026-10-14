package com.acme.transport.linehaul.backhaul.cappedbackhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingBackhaulScoreTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingBackhaulScore("code-39", "title-55")
        assertEquals(PendingBackhaulScoreStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPendingBackhaulScore("code-39", "title-55")
        try {
            item.moveTo(PendingBackhaulScoreStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingBackhaulScoreStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingBackhaulScore("code-39", "title-55")
        item.status = PendingBackhaulScoreStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingBackhaulScoreStatus.REQUESTED) }
    }
}
