package com.acme.transport.customs.declaration.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDeclarationCheckpointTest {
    @Test
    fun `starts in idle`() {
        val item = newLenientDeclarationCheckpoint("code-674", "label-973")
        assertEquals(LenientDeclarationCheckpointStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newLenientDeclarationCheckpoint("code-674", "label-973")
        try {
            item.moveTo(LenientDeclarationCheckpointStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDeclarationCheckpointStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLenientDeclarationCheckpoint("code-674", "label-973")
        item.status = LenientDeclarationCheckpointStatus.STOPPED
        assertFails("move") { item.moveTo(LenientDeclarationCheckpointStatus.IDLE) }
    }
}
