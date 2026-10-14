package com.acme.transport.crewing.roster.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryRosterRecordTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryRosterRecord("code-863", "name-187")
        assertEquals(PrimaryRosterRecordStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPrimaryRosterRecord("code-863", "name-187")
        try {
            item.moveTo(PrimaryRosterRecordStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryRosterRecordStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryRosterRecord("code-863", "name-187")
        item.status = PrimaryRosterRecordStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryRosterRecordStatus.REQUESTED) }
    }
}
