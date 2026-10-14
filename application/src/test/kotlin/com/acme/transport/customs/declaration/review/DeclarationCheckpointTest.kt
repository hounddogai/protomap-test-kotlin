package com.acme.transport.customs.declaration.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationCheckpointTest {
    @Test
    fun `starts in created`() {
        val item = newDeclarationCheckpoint("code-697", "title-488")
        assertEquals(DeclarationCheckpointStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newDeclarationCheckpoint("code-697", "title-488")
        try {
            item.moveTo(DeclarationCheckpointStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationCheckpointStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDeclarationCheckpoint("code-697", "title-488")
        item.status = DeclarationCheckpointStatus.RETIRED
        assertFails("move") { item.moveTo(DeclarationCheckpointStatus.CREATED) }
    }
}
