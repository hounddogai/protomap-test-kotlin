package com.acme.transport.network.transittime.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictTransitTimeBaselineTest {
    @Test
    fun `starts in new`() {
        val item = newStrictTransitTimeBaseline("code-251", "label-943")
        assertEquals(StrictTransitTimeBaselineStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newStrictTransitTimeBaseline("code-251", "label-943")
        try {
            item.moveTo(StrictTransitTimeBaselineStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictTransitTimeBaselineStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newStrictTransitTimeBaseline("code-251", "label-943")
        item.status = StrictTransitTimeBaselineStatus.FAILED
        assertFails("move") { item.moveTo(StrictTransitTimeBaselineStatus.NEW) }
    }
}
