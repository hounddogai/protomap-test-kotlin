package com.acme.transport.safety.nearmiss.rollingmissrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveNearMissExceptionTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveNearMissException("code-210", "name-917")
        assertEquals(AdaptiveNearMissExceptionStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newAdaptiveNearMissException("code-210", "name-917")
        try {
            item.moveTo(AdaptiveNearMissExceptionStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveNearMissExceptionStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveNearMissException("code-210", "name-917")
        item.status = AdaptiveNearMissExceptionStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveNearMissExceptionStatus.NEW) }
    }
}
