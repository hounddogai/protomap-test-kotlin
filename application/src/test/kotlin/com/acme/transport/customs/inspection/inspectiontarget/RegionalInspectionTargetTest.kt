package com.acme.transport.customs.inspection.inspectiontarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalInspectionTargetTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalInspectionTarget("code-266", "name-871")
        assertEquals(RegionalInspectionTargetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRegionalInspectionTarget("code-266", "name-871")
        try {
            item.moveTo(RegionalInspectionTargetStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalInspectionTargetStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalInspectionTarget("code-266", "name-871")
        item.status = RegionalInspectionTargetStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalInspectionTargetStatus.REQUESTED) }
    }
}
