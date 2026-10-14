package com.acme.transport.network.node.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryNodeVarianceTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryNodeVariance("code-81", "title-838")
        assertEquals(PrimaryNodeVarianceStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newPrimaryNodeVariance("code-81", "title-838")
        try {
            item.moveTo(PrimaryNodeVarianceStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryNodeVarianceStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryNodeVariance("code-81", "title-838")
        item.status = PrimaryNodeVarianceStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryNodeVarianceStatus.OPEN) }
    }
}
