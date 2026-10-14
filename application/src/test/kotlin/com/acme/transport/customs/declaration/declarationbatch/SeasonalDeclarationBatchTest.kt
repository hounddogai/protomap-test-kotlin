package com.acme.transport.customs.declaration.declarationbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDeclarationBatchTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalDeclarationBatch("code-315", "title-684")
        assertEquals(SeasonalDeclarationBatchStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newSeasonalDeclarationBatch("code-315", "title-684")
        try {
            item.moveTo(SeasonalDeclarationBatchStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDeclarationBatchStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalDeclarationBatch("code-315", "title-684")
        item.status = SeasonalDeclarationBatchStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalDeclarationBatchStatus.OPEN) }
    }
}
