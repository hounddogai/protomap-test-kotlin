package com.acme.transport.crewing.qualification.qualificationtemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryQualificationLedgerTest {
    @Test
    fun `starts in created`() {
        val item = newPrimaryQualificationLedger("code-40", "title-531")
        assertEquals(PrimaryQualificationLedgerStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newPrimaryQualificationLedger("code-40", "title-531")
        try {
            item.moveTo(PrimaryQualificationLedgerStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryQualificationLedgerStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPrimaryQualificationLedger("code-40", "title-531")
        item.status = PrimaryQualificationLedgerStatus.RETIRED
        assertFails("move") { item.moveTo(PrimaryQualificationLedgerStatus.CREATED) }
    }
}
