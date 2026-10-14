package com.acme.transport.network.zone.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneExceptionTest {
    @Test
    fun `starts in proposed`() {
        val item = newZoneException("code-4", "label-384")
        assertEquals(ZoneExceptionStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newZoneException("code-4", "label-384")
        try {
            item.moveTo(ZoneExceptionStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneExceptionStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newZoneException("code-4", "label-384")
        item.status = ZoneExceptionStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ZoneExceptionStatus.PROPOSED) }
    }
}
