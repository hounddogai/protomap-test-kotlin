package com.acme.transport.crewing.qualification.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyQualificationQuotaTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyQualificationQuota("code-897", "title-122")
        assertEquals(LegacyQualificationQuotaStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newLegacyQualificationQuota("code-897", "title-122")
        try {
            item.moveTo(LegacyQualificationQuotaStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyQualificationQuotaStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyQualificationQuota("code-897", "title-122")
        item.status = LegacyQualificationQuotaStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyQualificationQuotaStatus.PROPOSED) }
    }
}
