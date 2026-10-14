package com.acme.transport.crewing.roster.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredRosterRecordTest {
    @Test
    fun `starts in new`() {
        val item = newTieredRosterRecord("code-587", "name-746")
        assertEquals(TieredRosterRecordStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newTieredRosterRecord("code-587", "name-746")
        try {
            item.moveTo(TieredRosterRecordStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredRosterRecordStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTieredRosterRecord("code-587", "name-746")
        item.status = TieredRosterRecordStatus.FAILED
        assertFails("move") { item.moveTo(TieredRosterRecordStatus.NEW) }
    }
}
