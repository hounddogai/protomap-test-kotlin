package com.acme.transport.crewing.qualification.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationLedgerTest {
    @Test
    fun `starts in created`() {
        val item = newQualificationLedger("code-784", "name-388")
        assertEquals(QualificationLedgerStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newQualificationLedger("code-784", "name-388")
        try {
            item.moveTo(QualificationLedgerStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationLedgerStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newQualificationLedger("code-784", "name-388")
        item.status = QualificationLedgerStatus.RETIRED
        assertFails("move") { item.moveTo(QualificationLedgerStatus.CREATED) }
    }
}
