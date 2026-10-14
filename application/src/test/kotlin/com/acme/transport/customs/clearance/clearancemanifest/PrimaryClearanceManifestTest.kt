package com.acme.transport.customs.clearance.clearancemanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryClearanceManifestTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryClearanceManifest("code-36", "title-546")
        assertEquals(PrimaryClearanceManifestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newPrimaryClearanceManifest("code-36", "title-546")
        try {
            item.moveTo(PrimaryClearanceManifestStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryClearanceManifestStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryClearanceManifest("code-36", "title-546")
        item.status = PrimaryClearanceManifestStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryClearanceManifestStatus.REQUESTED) }
    }
}
