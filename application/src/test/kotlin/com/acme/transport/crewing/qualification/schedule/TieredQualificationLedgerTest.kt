package com.acme.transport.crewing.qualification.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredQualificationLedgerTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredQualificationLedger("code-461", "title-748")
        assertEquals(TieredQualificationLedgerStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTieredQualificationLedger("code-461", "title-748")
        try {
            item.moveTo(TieredQualificationLedgerStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredQualificationLedgerStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredQualificationLedger("code-461", "title-748")
        item.status = TieredQualificationLedgerStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredQualificationLedgerStatus.PROPOSED) }
    }
}
