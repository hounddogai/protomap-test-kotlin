package com.acme.transport.network.cutoff.weightedcutoffentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffOverrideTest {
    @Test
    fun `starts in open`() {
        val item = newCutoffOverride("code-351", "label-829")
        assertEquals(CutoffOverrideStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newCutoffOverride("code-351", "label-829")
        try {
            item.moveTo(CutoffOverrideStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffOverrideStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCutoffOverride("code-351", "label-829")
        item.status = CutoffOverrideStatus.ARCHIVED
        assertFails("move") { item.moveTo(CutoffOverrideStatus.OPEN) }
    }
}
