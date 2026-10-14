package com.acme.transport.network.node.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientNodeManifestTest {
    @Test
    fun `starts in requested`() {
        val item = newLenientNodeManifest("code-783", "name-868")
        assertEquals(LenientNodeManifestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLenientNodeManifest("code-783", "name-868")
        try {
            item.moveTo(LenientNodeManifestStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientNodeManifestStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLenientNodeManifest("code-783", "name-868")
        item.status = LenientNodeManifestStatus.EXPIRED
        assertFails("move") { item.moveTo(LenientNodeManifestStatus.REQUESTED) }
    }
}
