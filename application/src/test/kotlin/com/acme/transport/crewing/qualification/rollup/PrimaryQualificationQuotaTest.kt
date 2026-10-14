package com.acme.transport.crewing.qualification.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryQualificationQuotaTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryQualificationQuota("code-427", "title-946")
        assertEquals(PrimaryQualificationQuotaStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newPrimaryQualificationQuota("code-427", "title-946")
        try {
            item.moveTo(PrimaryQualificationQuotaStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryQualificationQuotaStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryQualificationQuota("code-427", "title-946")
        item.status = PrimaryQualificationQuotaStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryQualificationQuotaStatus.OPEN) }
    }
}
