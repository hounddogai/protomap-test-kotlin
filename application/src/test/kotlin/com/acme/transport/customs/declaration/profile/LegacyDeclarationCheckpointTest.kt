package com.acme.transport.customs.declaration.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyDeclarationCheckpointTest {
    @Test
    fun `starts in requested`() {
        val item = newLegacyDeclarationCheckpoint("code-540", "name-493")
        assertEquals(LegacyDeclarationCheckpointStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newLegacyDeclarationCheckpoint("code-540", "name-493")
        try {
            item.moveTo(LegacyDeclarationCheckpointStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyDeclarationCheckpointStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLegacyDeclarationCheckpoint("code-540", "name-493")
        item.status = LegacyDeclarationCheckpointStatus.EXPIRED
        assertFails("move") { item.moveTo(LegacyDeclarationCheckpointStatus.REQUESTED) }
    }
}
