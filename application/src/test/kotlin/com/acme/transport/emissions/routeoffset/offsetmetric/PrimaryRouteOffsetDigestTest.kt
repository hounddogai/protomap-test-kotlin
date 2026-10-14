package com.acme.transport.emissions.routeoffset.offsetmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryRouteOffsetDigestTest {
    @Test
    fun `starts in idle`() {
        val item = newPrimaryRouteOffsetDigest("code-440", "name-29")
        assertEquals(PrimaryRouteOffsetDigestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newPrimaryRouteOffsetDigest("code-440", "name-29")
        try {
            item.moveTo(PrimaryRouteOffsetDigestStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryRouteOffsetDigestStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPrimaryRouteOffsetDigest("code-440", "name-29")
        item.status = PrimaryRouteOffsetDigestStatus.STOPPED
        assertFails("move") { item.moveTo(PrimaryRouteOffsetDigestStatus.IDLE) }
    }
}
