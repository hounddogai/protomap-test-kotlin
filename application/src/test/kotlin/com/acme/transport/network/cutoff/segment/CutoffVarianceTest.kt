package com.acme.transport.network.cutoff.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffVarianceTest {
    @Test
    fun `starts in open`() {
        val item = newCutoffVariance("code-364", "name-394")
        assertEquals(CutoffVarianceStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newCutoffVariance("code-364", "name-394")
        try {
            item.moveTo(CutoffVarianceStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffVarianceStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCutoffVariance("code-364", "name-394")
        item.status = CutoffVarianceStatus.ARCHIVED
        assertFails("move") { item.moveTo(CutoffVarianceStatus.OPEN) }
    }
}
