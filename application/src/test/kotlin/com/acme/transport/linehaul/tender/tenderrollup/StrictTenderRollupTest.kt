package com.acme.transport.linehaul.tender.tenderrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictTenderRollupTest {
    @Test
    fun `starts in open`() {
        val item = newStrictTenderRollup("code-553", "label-340")
        assertEquals(StrictTenderRollupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newStrictTenderRollup("code-553", "label-340")
        try {
            item.moveTo(StrictTenderRollupStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictTenderRollupStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictTenderRollup("code-553", "label-340")
        item.status = StrictTenderRollupStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictTenderRollupStatus.OPEN) }
    }
}
