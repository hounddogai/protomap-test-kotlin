package com.acme.transport.fleetops.utilization.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationDigestTest {
    @Test
    fun `starts in new`() {
        val item = newUtilizationDigest("code-505", "title-503")
        assertEquals(UtilizationDigestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newUtilizationDigest("code-505", "title-503")
        try {
            item.moveTo(UtilizationDigestStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationDigestStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newUtilizationDigest("code-505", "title-503")
        item.status = UtilizationDigestStatus.FAILED
        assertFails("move") { item.moveTo(UtilizationDigestStatus.NEW) }
    }
}
