package com.acme.transport.terminal.crossdock.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockVarianceTest {
    @Test
    fun `starts in draft`() {
        val item = newCrossDockVariance("code-98", "name-619")
        assertEquals(CrossDockVarianceStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newCrossDockVariance("code-98", "name-619")
        try {
            item.moveTo(CrossDockVarianceStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockVarianceStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCrossDockVariance("code-98", "name-619")
        item.status = CrossDockVarianceStatus.CLOSED
        assertFails("move") { item.moveTo(CrossDockVarianceStatus.DRAFT) }
    }
}
