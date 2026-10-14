package com.acme.transport.customs.inspection.inspectiondigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalInspectionDigestTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalInspectionDigest("code-78", "label-684")
        assertEquals(SeasonalInspectionDigestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newSeasonalInspectionDigest("code-78", "label-684")
        try {
            item.moveTo(SeasonalInspectionDigestStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalInspectionDigestStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalInspectionDigest("code-78", "label-684")
        item.status = SeasonalInspectionDigestStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalInspectionDigestStatus.REQUESTED) }
    }
}
