package com.acme.transport.lastmile.attempt.attemptclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedAttemptRecordTest {
    @Test
    fun `starts in created`() {
        val item = newCappedAttemptRecord("code-728", "name-231")
        assertEquals(CappedAttemptRecordStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newCappedAttemptRecord("code-728", "name-231")
        try {
            item.moveTo(CappedAttemptRecordStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedAttemptRecordStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCappedAttemptRecord("code-728", "name-231")
        item.status = CappedAttemptRecordStatus.RETIRED
        assertFails("move") { item.moveTo(CappedAttemptRecordStatus.CREATED) }
    }
}
