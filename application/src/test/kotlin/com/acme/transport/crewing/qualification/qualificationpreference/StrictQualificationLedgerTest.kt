package com.acme.transport.crewing.qualification.qualificationpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictQualificationLedgerTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictQualificationLedger("code-464", "name-34")
        assertEquals(StrictQualificationLedgerStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newStrictQualificationLedger("code-464", "name-34")
        try {
            item.moveTo(StrictQualificationLedgerStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictQualificationLedgerStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictQualificationLedger("code-464", "name-34")
        item.status = StrictQualificationLedgerStatus.CLOSED
        assertFails("move") { item.moveTo(StrictQualificationLedgerStatus.DRAFT) }
    }
}
