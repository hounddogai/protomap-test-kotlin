package com.acme.transport.network.cutoff.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffBaselineTest {
    @Test
    fun `starts in open`() {
        val item = newCutoffBaseline("code-639", "label-288")
        assertEquals(CutoffBaselineStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newCutoffBaseline("code-639", "label-288")
        try {
            item.moveTo(CutoffBaselineStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffBaselineStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCutoffBaseline("code-639", "label-288")
        item.status = CutoffBaselineStatus.ARCHIVED
        assertFails("move") { item.moveTo(CutoffBaselineStatus.OPEN) }
    }
}
