package com.acme.transport.lastmile.drop.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryDropRecordTest {
    @Test
    fun `starts in proposed`() {
        val item = newPrimaryDropRecord("code-467", "name-666")
        assertEquals(PrimaryDropRecordStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newPrimaryDropRecord("code-467", "name-666")
        try {
            item.moveTo(PrimaryDropRecordStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryDropRecordStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPrimaryDropRecord("code-467", "name-666")
        item.status = PrimaryDropRecordStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PrimaryDropRecordStatus.PROPOSED) }
    }
}
