package com.acme.transport.customs.declaration.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedDeclarationCheckpointTest {
    @Test
    fun `starts in open`() {
        val item = newWeightedDeclarationCheckpoint("code-519", "name-961")
        assertEquals(WeightedDeclarationCheckpointStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newWeightedDeclarationCheckpoint("code-519", "name-961")
        try {
            item.moveTo(WeightedDeclarationCheckpointStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedDeclarationCheckpointStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newWeightedDeclarationCheckpoint("code-519", "name-961")
        item.status = WeightedDeclarationCheckpointStatus.ARCHIVED
        assertFails("move") { item.moveTo(WeightedDeclarationCheckpointStatus.OPEN) }
    }
}
