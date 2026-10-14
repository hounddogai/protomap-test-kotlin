package com.acme.transport.safety.violation.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationManifestTest {
    @Test
    fun `starts in requested`() {
        val item = newViolationManifest("code-977", "name-508")
        assertEquals(ViolationManifestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newViolationManifest("code-977", "name-508")
        try {
            item.moveTo(ViolationManifestStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationManifestStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newViolationManifest("code-977", "name-508")
        item.status = ViolationManifestStatus.EXPIRED
        assertFails("move") { item.moveTo(ViolationManifestStatus.REQUESTED) }
    }
}
