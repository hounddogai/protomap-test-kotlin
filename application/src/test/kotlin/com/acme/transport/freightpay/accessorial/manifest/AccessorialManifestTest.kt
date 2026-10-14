package com.acme.transport.freightpay.accessorial.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialManifestTest {
    @Test
    fun `starts in requested`() {
        val item = newAccessorialManifest("code-653", "title-535")
        assertEquals(AccessorialManifestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newAccessorialManifest("code-653", "title-535")
        try {
            item.moveTo(AccessorialManifestStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialManifestStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAccessorialManifest("code-653", "title-535")
        item.status = AccessorialManifestStatus.EXPIRED
        assertFails("move") { item.moveTo(AccessorialManifestStatus.REQUESTED) }
    }
}
