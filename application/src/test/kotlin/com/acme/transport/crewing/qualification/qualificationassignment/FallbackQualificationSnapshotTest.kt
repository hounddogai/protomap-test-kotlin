package com.acme.transport.crewing.qualification.qualificationassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackQualificationSnapshotTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackQualificationSnapshot("code-477", "title-317")
        assertEquals(FallbackQualificationSnapshotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newFallbackQualificationSnapshot("code-477", "title-317")
        try {
            item.moveTo(FallbackQualificationSnapshotStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackQualificationSnapshotStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackQualificationSnapshot("code-477", "title-317")
        item.status = FallbackQualificationSnapshotStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackQualificationSnapshotStatus.IDLE) }
    }
}
