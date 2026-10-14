package com.acme.transport.linehaul.tender.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderManifestTest {
    @Test
    fun `starts in new`() {
        val item = newTenderManifest("code-579", "name-247")
        assertEquals(TenderManifestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newTenderManifest("code-579", "name-247")
        try {
            item.moveTo(TenderManifestStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderManifestStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTenderManifest("code-579", "name-247")
        item.status = TenderManifestStatus.FAILED
        assertFails("move") { item.moveTo(TenderManifestStatus.NEW) }
    }
}
