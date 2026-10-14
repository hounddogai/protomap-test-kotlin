package com.acme.transport.fleetops.tire.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveTireExceptionTest {
    @Test
    fun `starts in created`() {
        val item = newAdaptiveTireException("code-338", "label-172")
        assertEquals(AdaptiveTireExceptionStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newAdaptiveTireException("code-338", "label-172")
        try {
            item.moveTo(AdaptiveTireExceptionStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveTireExceptionStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAdaptiveTireException("code-338", "label-172")
        item.status = AdaptiveTireExceptionStatus.RETIRED
        assertFails("move") { item.moveTo(AdaptiveTireExceptionStatus.CREATED) }
    }
}
