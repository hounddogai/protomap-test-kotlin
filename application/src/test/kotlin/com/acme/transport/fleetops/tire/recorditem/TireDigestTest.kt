package com.acme.transport.fleetops.tire.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireDigestTest {
    @Test
    fun `starts in planned`() {
        val item = newTireDigest("code-386", "name-625")
        assertEquals(TireDigestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newTireDigest("code-386", "name-625")
        try {
            item.moveTo(TireDigestStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireDigestStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTireDigest("code-386", "name-625")
        item.status = TireDigestStatus.CANCELLED
        assertFails("move") { item.moveTo(TireDigestStatus.PLANNED) }
    }
}
