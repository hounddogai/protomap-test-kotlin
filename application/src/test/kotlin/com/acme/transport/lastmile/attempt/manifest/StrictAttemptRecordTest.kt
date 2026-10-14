package com.acme.transport.lastmile.attempt.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictAttemptRecordTest {
    @Test
    fun `starts in proposed`() {
        val item = newStrictAttemptRecord("code-679", "name-234")
        assertEquals(StrictAttemptRecordStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newStrictAttemptRecord("code-679", "name-234")
        try {
            item.moveTo(StrictAttemptRecordStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictAttemptRecordStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newStrictAttemptRecord("code-679", "name-234")
        item.status = StrictAttemptRecordStatus.WITHDRAWN
        assertFails("move") { item.moveTo(StrictAttemptRecordStatus.PROPOSED) }
    }
}
