package com.acme.transport.crewing.roster.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientRosterBatchTest {
    @Test
    fun `starts in created`() {
        val item = newLenientRosterBatch("code-94", "label-359")
        assertEquals(LenientRosterBatchStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newLenientRosterBatch("code-94", "label-359")
        try {
            item.moveTo(LenientRosterBatchStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientRosterBatchStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientRosterBatch("code-94", "label-359")
        item.status = LenientRosterBatchStatus.RETIRED
        assertFails("move") { item.moveTo(LenientRosterBatchStatus.CREATED) }
    }
}
