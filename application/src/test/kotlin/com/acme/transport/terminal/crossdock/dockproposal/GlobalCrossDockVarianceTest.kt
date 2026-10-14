package com.acme.transport.terminal.crossdock.dockproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalCrossDockVarianceTest {
    @Test
    fun `starts in planned`() {
        val item = newGlobalCrossDockVariance("code-194", "title-948")
        assertEquals(GlobalCrossDockVarianceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newGlobalCrossDockVariance("code-194", "title-948")
        try {
            item.moveTo(GlobalCrossDockVarianceStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalCrossDockVarianceStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newGlobalCrossDockVariance("code-194", "title-948")
        item.status = GlobalCrossDockVarianceStatus.CANCELLED
        assertFails("move") { item.moveTo(GlobalCrossDockVarianceStatus.PLANNED) }
    }
}
